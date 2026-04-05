package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.application.delivery.service.GetTodayCompanyDeliveryRoutesService;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.CompanyDeliveryPoint;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VehicleRoute;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwInput;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwResult;
import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryAssignmentDomainService;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.CompanyInfoDto;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final RouteOptimizationService routeOptimizationService;
    private final GetTodayCompanyDeliveryRoutesService getTodayCompanyDeliveryRoutesService;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryAssignmentDomainService deliveryAssignmentDomainService;
    private final CompanyServiceClient companyServiceClient;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;


    public void assignDailyCompanyDeliveries(UUID hubId) {

        List<TodayCompanyDeliveryRouteResponseDto> todayDeliveries = getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId);
        // 오늘 배송 건이 없으면
        if (todayDeliveries.isEmpty()) {
            return;
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
        VrptwResult result = routeOptimizationService.solve(input);


        if (!result.feasible()) {
            log.warn("VRPTW 시간 윈도우 충족 불가 - hubId={}, 제약 완화 재시도", hubId);
            List<CompanyDeliveryPoint> relaxedPoints = companyDeliveryRoute.stream()
                    .map(p -> CompanyDeliveryPoint.of(
                            p.deliveryId(), p.deliveryRouteRecordId(), p.destinationId(),
                            p.distanceFromHubKm(), p.durationFromHubMin(), null,
                            p.latitude(), p.longitude()))
                    .toList();
            input = VrptwInput.of(relaxedPoints, availableManagers.size(), batchStartTime);
            result = routeOptimizationService.solve(input);
            if (!result.feasible()) {
                log.error("VRPTW 제약 완화 후에도 해를 찾지 못함 - hubId={}", hubId);
                throw new CustomException(ErrorCode.ROUTE_OPTIMIZATION_FAILED);
            }
        }

        // 매니저 배정 (대기시간 긴 매니저 → 이동거리 긴 경로 우선 배정)
        List<VehicleRoute> routes = result.routes().stream()
                .sorted(Comparator.comparingDouble(VehicleRoute::distanceKm).reversed())
                .toList();

        List<DeliveryManager> sortedManagers = deliveryAssignmentDomainService.rankManagersByPriority(availableManagers);

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

                // 배정된 담당자로 delivery, deliveryRouteRecord 업데이트
                UUID managerId = manager.getDeliveryManagerId().getDeliveryManagerId();

                Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(point.deliveryId())
                        .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
                delivery.updateManagerId(managerId);

                DeliveryRouteRecord routeRecord = deliveryRouteRecordRepository
                        .findByDeliveryRouteRecordIdAndDeletedAtIsNull(point.deliveryRouteRecordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ROUTE_RECORD_NOT_FOUND));
                routeRecord.updateManagerId(managerId);

                assignedCount++;
            }
        }
        log.info("배송 일괄 배정 완료 - hubId={}, 배송건={}, 매니저={}, 경로={}",
                hubId, assignedCount,
                Math.min(availableManagers.size(), routes.size()),
                routes.size());
    }

    private List<CompanyDeliveryPoint> getCompanyDeliveryRoute(List<TodayCompanyDeliveryRouteResponseDto> todayDeliveries) {
        // 당일 배송 업체 ID List
        List<UUID> companyIds = todayDeliveries.stream()
                .map(TodayCompanyDeliveryRouteResponseDto::destinationId)
                .distinct().toList();

        Map<UUID, CompanyInfoDto> companyMap = companyServiceClient.getCompanies(companyIds).stream()
                .collect(Collectors.toMap(CompanyInfoDto::companyId, Function.identity()));

        return todayDeliveries.stream()
                .map(r -> {
                    CompanyInfoDto company = companyMap.get(r.destinationId());
                    return CompanyDeliveryPoint.of(
                            r.deliveryId(),
                            r.deliveryRouteRecordId(),
                            r.destinationId(),
                            r.expectedDistanceKm(),
                            r.expectedDurationMin(),
                            r.desiredDeliveryAt(),
                            company.latitude(),
                            company.longitude()
                    );
                }).toList();
    }

}
