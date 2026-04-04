package com.da2jobu.deliveryservice.application.delivery.dto;

import java.util.Objects;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy
) {
    public OrderAcceptedEvent {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(supplierId, "supplierId must not be null");
        Objects.requireNonNull(receiverId, "receiverId must not be null");

        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy must not be blank");
        }
    }
}