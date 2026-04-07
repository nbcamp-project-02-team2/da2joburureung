package com.da2jobu.deliveryservice.application.delivery.event;

import java.util.UUID;

public record DeliveryCreatedEvent(
        UUID orderId,
        UUID deliveryId
) {
}
