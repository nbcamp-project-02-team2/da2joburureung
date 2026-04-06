package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HubInfoDto(
        UUID hubId,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
