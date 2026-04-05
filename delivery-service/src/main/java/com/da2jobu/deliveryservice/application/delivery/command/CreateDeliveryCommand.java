package com.da2jobu.deliveryservice.application.delivery.command;

import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateDeliveryCommand(
        UUID orderId,
        UUID originHubId,
        UUID destinationHubId,
        String deliveryAddress,
        String receiverName,
        String receiverSlackId,
        UUID companyDeliveryManagerId,
        String requestNote,
        Integer expectedDurationTotalMin,
        DeliveryStatus status,
        LocalDateTime desiredDeliveryAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
