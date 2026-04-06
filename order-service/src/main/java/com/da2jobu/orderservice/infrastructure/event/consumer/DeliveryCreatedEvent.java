package com.da2jobu.orderservice.infrastructure.event.consumer;

import java.util.UUID;

public record DeliveryCreatedEvent(
        UUID orderId,
        UUID deliveryId
) {}
