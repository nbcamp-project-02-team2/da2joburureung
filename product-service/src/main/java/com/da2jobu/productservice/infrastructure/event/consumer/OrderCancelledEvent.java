package com.da2jobu.productservice.infrastructure.event.consumer;

import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        UUID productId,
        Integer quantity
) {}
