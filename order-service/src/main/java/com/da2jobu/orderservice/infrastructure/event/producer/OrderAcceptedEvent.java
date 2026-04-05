package com.da2jobu.orderservice.infrastructure.event.producer;

import java.time.LocalDate;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy,
        LocalDate desiredDeliveryDate
) {}
