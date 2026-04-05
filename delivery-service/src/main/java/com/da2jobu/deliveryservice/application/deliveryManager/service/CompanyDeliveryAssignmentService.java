package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.CompanyDeliveryPoint;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VehicleRoute;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwInput;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwResult;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.CompanyDeliveryAssignmentResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.ManagerIdleDuration;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.infrastructure.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.CompanyInfoDto;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDeliveryAssignmentService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final VrptwSolverService vrptwSolverService;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final CompanyServiceClient companyServiceClient;


    public CompanyDeliveryAssignmentResult assignDailyCompanyDeliveries(UUID hubId) {

        /**
         * todo : 당일 배송 예정 건 일괄 조회
         */
        List<DeliveryRouteRecord> todayDeliveries = new ArrayList<>();
        // 오늘 배송 건이 없으면
        if (todayDeliveries.isEmpty()) {
            return CompanyDeliveryAssignmentResult.of(hubId, 0, 0, 0);
        }

        // 해당 허브 소속 가용 업체배송 매니저 조회
        List<DeliveryManager> availableManagers = deliveryManagerRepository
                .findAvailableCompanyManagersByHub(HubId.of(hubId));

        if (availableManagers.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_COMPANY_DELIVERY_MANAGER);
        }

        // 배송 routeRecord 추출 (업체 좌표 포함)
        List<CompanyDeliveryPoint> companyDeliveryRoute = getCompanyDeliveryRoute(todayDeliveries);

        // OR-Tools VRPTW 솔버 (클러스터링 + 경로 + 시간 윈도우 통합)
        LocalDateTime batchStartTime = LocalDate.now().atTime(6, 0);
        VrptwInput input = VrptwInput.of(companyDeliveryRoute, availableManagers.size(), batchStartTime);
        VrptwResult result = vrptwSolverService.solve(input);


        if (!result.feasible()) {
            log.warn("VRPTW 시간 윈도우 충족 불가 - hubId={}, 제약 완화 재시도", hubId);
            List<CompanyDeliveryPoint> relaxedPoints = companyDeliveryRoute.stream()
                    .map(p -> CompanyDeliveryPoint.of(
                            p.deliveryId(), p.deliveryRouteRecordId(), p.destinationId(),
                            p.distanceFromHubKm(), p.durationFromHubMin(), null,
                            p.latitude(), p.longitude()))
                    .toList();
            input = VrptwInput.of(relaxedPoints, availableManagers.size(), batchStartTime);
            result = vrptwSolverService.solve(input);
        }

        // 매니저 배정 (대기시간 긴 매니저 → 이동거리 긴 경로 우선 배정)
        List<VehicleRoute> routes = result.routes().stream()
                .sorted(Comparator.comparingDouble(VehicleRoute::distanceKm).reversed())
                .toList();

        List<DeliveryManager> sortedManagers = sortManagersByIdleDuration(availableManagers);

        int assignedCount = 0;
        for (int i = 0; i < routes.size(); i++) {
            DeliveryManager manager = sortedManagers.get(i % sortedManagers.size());
            for (CompanyDeliveryPoint point : routes.get(i).orderedDeliveries()) {
                DeliveryAssignment assignment = DeliveryAssignment.create(
                        manager.getDeliveryManagerId(),
                        DeliveryId.of(point.deliveryId()),
                        DeliveryRouteRecordId.of(point.deliveryRouteRecordId()),
                        HubId.of(hubId)
                );
                deliveryAssignmentRepository.save(assignment);
                assignedCount++;
            }
        }
        log.info("배송 일괄 배정 완료 - hubId={}, 배송건={}, 매니저={}, 경로={}",
                hubId, assignedCount,
                Math.min(availableManagers.size(), routes.size()),
                routes.size());

        return new CompanyDeliveryAssignmentResult(
                hubId, assignedCount,
                Math.min(availableManagers.size(), routes.size()),
                routes.size()
        );
    }

    private List<CompanyDeliveryPoint> getCompanyDeliveryRoute(List<DeliveryRouteRecord> todayDeliveries) {
        // 당일 배송 업체 ID List
        List<UUID> companyIds = todayDeliveries.stream()
                .map(DeliveryRouteRecord::getDestinationId)
                .distinct().toList();

        Map<UUID, CompanyInfoDto> companyMap = companyServiceClient.getCompanies(companyIds).stream()
                .collect(Collectors.toMap(CompanyInfoDto::companyId, Function.identity()));

        return todayDeliveries.stream()
                .map(r -> {
                    CompanyInfoDto company = companyMap.get(r.getDestinationId());
                    return CompanyDeliveryPoint.of(
                            r.getDeliveryId(),
                            r.getDeliveryRouteRecordId(),
                            r.getDestinationId(),
                            r.getExpectedDistanceKm(),
                            r.getExpectedDurationMin(),
                            /**
                             * 수령 희망일 : 임시 LocalDateTime.now()
                             */
                            LocalDateTime.now(),
                            company.latitude(),
                            company.longitude()
                    );
                }).toList();
    }

    /**
     * 업체 매니저
     * 대기시간(idle duration) 내림차순, seq 오름차순
     */
    private List<DeliveryManager> sortManagersByIdleDuration(List<DeliveryManager> managers) {
        List<DeliveryManagerId> managerIds = managers.stream()
                .map(DeliveryManager::getDeliveryManagerId).toList();

        Map<DeliveryManagerId, Duration> idleDurationMap = deliveryAssignmentRepository
                .findIdleDurationsByManagerIds(managerIds).stream()
                .collect(Collectors.toMap(
                        ManagerIdleDuration::managerId, ManagerIdleDuration::idleDuration));

        // 배정 이력이 없는 매니저는 createdAt 기준 대기시간 계산
        for (DeliveryManager manager : managers) {
            idleDurationMap.computeIfAbsent(manager.getDeliveryManagerId(),
                    id -> Duration.between(manager.getCreatedAt(), LocalDateTime.now()));
        }

        return managers.stream()
                .sorted(Comparator
                        .<DeliveryManager, Duration>comparing(
                                m -> idleDurationMap.get(m.getDeliveryManagerId()))
                        .reversed()
                        .thenComparing(DeliveryManager::getSeq))
                .toList();
    }
}
