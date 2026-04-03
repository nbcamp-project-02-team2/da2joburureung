package com.da2jobu.delivery_route_recordservice.application.dto;


import com.da2jobu.delivery_route_recordservice.domain.entity.DeliveryRouteRecord;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateDeliveryRouteMetricsResponseDto(
        UUID routeRecordId,
        BigDecimal actualDistanceKm,
        Integer actualDurationMin,
        Integer remainDurationMin
) {
    public static UpdateDeliveryRouteMetricsResponseDto from(DeliveryRouteRecord record) {
        return new UpdateDeliveryRouteMetricsResponseDto(
                record.getDeliveryRouteRecordId(),
                record.getRealDistanceKm(),
                record.getRealDurationMin(),
                record.getRemainDurationMin()
        );
    }
}
