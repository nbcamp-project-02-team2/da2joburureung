package com.da2jobu.deliveryservice.application.deliveryRouteRecord.command;

import java.math.BigDecimal;

public record UpdateDeliveryRouteMetricsCommand(
        BigDecimal actualDistanceKm,
        Integer actualDurationMin,
        Integer remainDurationMin
) {
}