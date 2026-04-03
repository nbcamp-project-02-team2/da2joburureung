package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;

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
