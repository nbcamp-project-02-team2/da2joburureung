package com.da2jobu.deliverymanagerservice.infrastructure.persistence;

import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryAssignmentRepository;
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