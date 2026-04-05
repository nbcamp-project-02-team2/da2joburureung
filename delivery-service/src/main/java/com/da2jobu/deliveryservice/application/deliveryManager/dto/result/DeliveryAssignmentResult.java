package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryAssignmentResult(
        UUID deliveryAssignmentId,
        UUID deliveryRouteRecordId,
        UUID deliveryId,
        DeliveryAssignmentStatus status,
        LocalDateTime createdAt
) {
    public static DeliveryAssignmentResult from(DeliveryAssignment assignment) {
        return new DeliveryAssignmentResult(
                assignment.getDeliveryAssignmentId().getDeliveryAssignmentId(),
                assignment.getDeliveryRouteRecordId().getDeliveryRouteRecordId(),
                assignment.getDeliveryId().getDeliveryId(),
                assignment.getStatus(),
                assignment.getCreatedAt()
        );
    }
}

