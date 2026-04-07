package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;

import java.math.BigDecimal;
import java.util.UUID;

public record LocationInfo(
        UUID id,
        RouteLocationType type,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
