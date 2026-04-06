package com.da2jobu.deliveryservice.application.delivery.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryPreparedEvent(
        UUID deliveryId,
        UUID orderId,

        String ordererName,
        String ordererEmail,
        LocalDateTime orderedAt,

        String productInfo,
        String requirements,

        DeliveryLocationPayload origin,
        List<DeliveryLocationPayload> waypoints,
        DeliveryLocationPayload destination,

        String deliveryManagerSlackId,
        String deliveryManagerName,
        String deliveryManagerEmail
) {
}
