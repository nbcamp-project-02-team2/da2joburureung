package com.da2jobu.deliveryservice.application.deliveryManager.service;


import com.da2jobu.deliveryservice.application.deliveryManager.dto.ManagerCandidate;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.HubDeliveryAssignmentResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.*;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubDeliveryAssignmentService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;

    /**
     * 허브 배송 담당자
     * 대기시간, 순번 기준 라운드로빈 방식
     * 배송 시작 지점까지 이동하는 것에 대한 시공간적 제약은 무시 (발제문 참고)
     */
    public HubDeliveryAssignmentResult assignHubDelivery(DeliveryId deliveryId, DeliveryRouteRecordId deliveryRouteRecordId, UUID startHubId) {
        List<DeliveryManager> availableHubDeliveryManagers = deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment();
        if (availableHubDeliveryManagers.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_NOT_FOUND);
        }
        Map<DeliveryManagerId, Duration> durationsForManagers = getIdleDurations(availableHubDeliveryManagers);

        List<ManagerCandidate> candidates = availableHubDeliveryManagers.stream()
                .map(manager -> ManagerCandidate.create(
                        manager,
                        durationsForManagers.get(manager.getDeliveryManagerId()),
                        manager.getSeq()
                ))
                .sorted(Comparator
                        .comparing(ManagerCandidate::idleDuration).reversed()
                        .thenComparing(ManagerCandidate::seq))
                .toList();

        // 가장 우선순위가 높은 배송담당자 선택
        ManagerCandidate selected = candidates.get(0);
        DeliveryManager manager = selected.manager();
        DeliveryAssignment assignment = DeliveryAssignment.create(
                manager.getDeliveryManagerId(),
                deliveryId,
                deliveryRouteRecordId,
                HubId.of(startHubId)
        );
        deliveryAssignmentRepository.save(assignment);
        return HubDeliveryAssignmentResult.from(assignment, manager);
    }

    // 각 배송담당자별로, 가장 최근에 완료(COMPLETED) 또는 취소(CANCELLED)된 배정건의 updatedAt 기준 경과 시간
    private Map<DeliveryManagerId, Duration> getIdleDurations(List<DeliveryManager> managers) {
        List<DeliveryManagerId> managerIds = managers.stream()
                .map(DeliveryManager::getDeliveryManagerId)
                .toList();

        List<ManagerIdleDuration> idleDurations = deliveryAssignmentRepository.findIdleDurationsByManagerIds(managerIds);

        Map<DeliveryManagerId, Duration> result = idleDurations.stream()
                .collect(Collectors.toMap(ManagerIdleDuration::managerId, ManagerIdleDuration::idleDuration));

        // 배정 이력이 없는 매니저는 createdAt 기준으로 대기시간 계산
        for (DeliveryManager manager : managers) {
            result.computeIfAbsent(manager.getDeliveryManagerId(),
                    id -> Duration.between(manager.getCreatedAt(), java.time.LocalDateTime.now()));
        }
        return result;
    }

    //배송 완료시 배정 상태 변경
    @Transactional
    public void completeHubDelivery(UUID deliveryAssignmentId) {
        DeliveryAssignment assignment = deliveryAssignmentRepository
                .findById(DeliveryAssignmentId.of(deliveryAssignmentId))
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ASSIGNMENT_NOT_FOUND));

        assignment.complete();

        log.info("배송 완료 - assignmentId={}, managerId={}",
                deliveryAssignmentId, assignment.getDeliveryManagerId().getDeliveryManagerId());
    }
}
