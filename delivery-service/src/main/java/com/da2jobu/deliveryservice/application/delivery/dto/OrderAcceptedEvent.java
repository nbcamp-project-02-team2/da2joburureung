package com.da2jobu.deliveryservice.application.delivery.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy,
        LocalDateTime desiredDeliveryAt
) {
    public OrderAcceptedEvent {
        Objects.requireNonNull(orderId, "orderId는 null이면 안됩니다.");
        Objects.requireNonNull(supplierId, "supplierId는 null이면 안됩니다.");
        Objects.requireNonNull(receiverId, "receiverId는 null이면 안됩니다.");
        Objects.requireNonNull(desiredDeliveryAt, "desiredDeliveryAt은 null이면 안됩니다.");

        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy는 blank면 안됩니다.");
        }
    }
}