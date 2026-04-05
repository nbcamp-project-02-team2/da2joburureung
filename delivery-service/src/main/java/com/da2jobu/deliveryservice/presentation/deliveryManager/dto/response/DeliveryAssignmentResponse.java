package com.da2jobu.deliveryservice.presentation.deliveryManager.dto.response;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryAssignmentResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryAssignmentResponse(
        UUID deliveryAssignmentId,
        UUID deliveryRouteRecordId,
        UUID deliveryId,
        DeliveryAssignmentStatus status,
        LocalDateTime createdAt
) {
    public static DeliveryAssignmentResponse from(DeliveryAssignmentResult result) {
        return new DeliveryAssignmentResponse(
                result.deliveryAssignmentId(),
                result.deliveryRouteRecordId(),
                result.deliveryId(),
                result.status(),
                result.createdAt()
        );
    }
}
