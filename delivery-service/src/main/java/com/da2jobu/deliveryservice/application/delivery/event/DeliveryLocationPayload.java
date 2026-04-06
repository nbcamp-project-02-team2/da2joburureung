package com.da2jobu.deliveryservice.application.delivery.event;

import java.math.BigDecimal;

public record DeliveryLocationPayload(
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
