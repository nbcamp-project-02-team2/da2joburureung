package com.da2jobu.deliveryservice.application.dto;

import com.da2jobu.deliveryservice.domain.entity.Delivery;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliverySummaryResponseDto(
        UUID deliveryId,
        UUID orderId,
        DeliveryStatus status,
        UUID originHubId,
        UUID destinationHubId,
        String deliveryAddress,
        String receiverName,
        UUID companyDeliveryManagerId,
        Integer expectedDurationTotalMin,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
    public static DeliverySummaryResponseDto from(Delivery delivery) {
        return new DeliverySummaryResponseDto(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getStatus(),
                delivery.getOriginHubId(),
                delivery.getDestinationHubId(),
                delivery.getDeliveryAddress(),
                delivery.getReceiverName(),
                delivery.getCompanyDeliveryManagerId(),
                delivery.getExpectedDurationTotalMin(),
                delivery.getStartedAt(),
                delivery.getCompletedAt()
        );
    }
}
