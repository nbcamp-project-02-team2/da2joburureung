package com.da2jobu.deliveryservice.application.delivery.command;

import java.util.UUID;

public record CreateDeliveryFromOrderCommand(   // order 이벤트에서 받은 원본 데이터
        UUID orderId,
        UUID supplierId,
        UUID receiverId,
        String requirements,
        String createdBy
) {
}
