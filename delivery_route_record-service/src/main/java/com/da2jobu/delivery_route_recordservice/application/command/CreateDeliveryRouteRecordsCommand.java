package com.da2jobu.delivery_route_recordservice.application.command;

import com.da2jobu.delivery_route_recordservice.domain.vo.RouteLocationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateDeliveryRouteRecordsCommand(
        UUID deliveryId,
        List<RouteItem> routes
) {
    public record RouteItem(
            Integer sequence,
            UUID originId,
            RouteLocationType originType,
            UUID destinationId,
            RouteLocationType destinationType,
            BigDecimal expectedDistanceKm,
            Integer expectedDurationMin,
            UUID deliveryManagerId,
            Integer remainDurationMin
    ) {
    }
}
