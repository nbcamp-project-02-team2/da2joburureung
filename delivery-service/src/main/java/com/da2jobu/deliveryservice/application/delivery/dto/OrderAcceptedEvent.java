package com.da2jobu.deliveryservice.application.delivery.dto;

import java.util.UUID;

public record OrderAcceptedEvent(   // 이벤트 payload
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy
) {
}
