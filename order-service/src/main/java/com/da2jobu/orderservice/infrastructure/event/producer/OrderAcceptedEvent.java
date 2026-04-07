package com.da2jobu.orderservice.infrastructure.event.producer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy,
        LocalDateTime desiredDeliveryAt
) {}
