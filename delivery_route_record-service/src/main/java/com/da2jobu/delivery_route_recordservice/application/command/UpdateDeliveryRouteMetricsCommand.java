package com.da2jobu.delivery_route_recordservice.application.command;

import java.math.BigDecimal;

public record UpdateDeliveryRouteMetricsCommand(
        BigDecimal actualDistanceKm,
        Integer actualDurationMin,
        Integer remainDurationMin
) {
}