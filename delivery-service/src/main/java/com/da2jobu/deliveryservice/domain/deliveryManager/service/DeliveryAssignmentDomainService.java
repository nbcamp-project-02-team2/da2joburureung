package com.da2jobu.deliveryservice.domain.deliveryManager.service;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.ManagerIdleDuration;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DeliveryAssignmentDomainService {

    private final DeliveryAssignmentRepository deliveryAssignmentRepository;

    /**
     * 배송 담당자 우선순위 정렬
     * 대기시간(idle duration) 내림차순, seq 오름차순
     */
    public List<DeliveryManager> rankManagersByPriority(List<DeliveryManager> managers) {
        Map<DeliveryManagerId, Duration> idleDurations = resolveIdleDurations(managers);

        return managers.stream()
                .sorted(Comparator
                        .<DeliveryManager, Duration>comparing(
                                m -> idleDurations.get(m.getDeliveryManagerId()))
                        .reversed()
                        .thenComparing(DeliveryManager::getSeq))
                .toList();
    }

    /**
     * 허브 배송 - 최적 배송 담당자 선정 (우선순위 1위)
     */
    public DeliveryManager selectBestCandidate(List<DeliveryManager> managers) {
        return rankManagersByPriority(managers).get(0);
    }

    /**
     * 각 배송담당자별 대기시간 계산
     * - 가장 최근 완료/취소 배정건의 updatedAt 기준 경과 시간
     * - 배정 이력이 없는 매니저는 createdAt 기준
     */
    private Map<DeliveryManagerId, Duration> resolveIdleDurations(List<DeliveryManager> managers) {
        List<DeliveryManagerId> managerIds = managers.stream()
                .map(DeliveryManager::getDeliveryManagerId).toList();

        Map<DeliveryManagerId, Duration> result = deliveryAssignmentRepository
                .findIdleDurationsByManagerIds(managerIds).stream()
                .collect(Collectors.toMap(
                        ManagerIdleDuration::managerId, ManagerIdleDuration::idleDuration));

        for (DeliveryManager manager : managers) {
            result.computeIfAbsent(manager.getDeliveryManagerId(),
                    id -> Duration.between(manager.getCreatedAt(), LocalDateTime.now()));
        }

        return result;
    }
}
