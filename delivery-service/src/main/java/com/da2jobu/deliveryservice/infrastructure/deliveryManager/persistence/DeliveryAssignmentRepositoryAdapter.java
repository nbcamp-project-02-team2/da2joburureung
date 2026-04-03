package com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DeliveryAssignmentRepositoryAdapter implements DeliveryAssignmentRepository {
    private final JpaDeliveryAssignmentRepository jpaDeliveryAssignmentRepository;

    @Override
    public boolean hasActiveDelivery(DeliveryManagerId deliveryManagerId) {
        return jpaDeliveryAssignmentRepository.existsByDeliveryManagerIdAndStatusIn(
                deliveryManagerId,
                List.of(DeliveryAssignmentStatus.ASSIGNED, DeliveryAssignmentStatus.PROGRESS)
        );
    }
}