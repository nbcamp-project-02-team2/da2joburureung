package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryDetailResponseDto(
        UUID deliveryId,
        UUID orderId,
        DeliveryStatus status,
        UUID originHubId,
        UUID destinationHubId,
        String deliveryAddress,
        String receiverName,
        String receiverSlackId,
        UUID companyDeliveryManagerId,
        String requestNote,
        Integer expectedDurationTotalMin,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static DeliveryDetailResponseDto from(Delivery delivery) {
        return new DeliveryDetailResponseDto(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getStatus(),
                delivery.getOriginHubId(),
                delivery.getDestinationHubId(),
                delivery.getDeliveryAddress(),
                delivery.getReceiverName(),
                delivery.getReceiverSlackId(),
                delivery.getCompanyDeliveryManagerId(),
                delivery.getRequestNote(),
                delivery.getExpectedDurationTotalMin(),
                delivery.getStartedAt(),
                delivery.getCompletedAt()
        );
    }
}
