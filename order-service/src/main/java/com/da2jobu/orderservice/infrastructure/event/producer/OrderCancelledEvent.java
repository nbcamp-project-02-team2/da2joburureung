package com.da2jobu.orderservice.infrastructure.event.producer;

import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID productId,
        Integer quantity
) {}
