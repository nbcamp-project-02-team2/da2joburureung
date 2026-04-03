package com.da2jobu.delivery_route_recordservice.application.dto;


import com.da2jobu.delivery_route_recordservice.domain.entity.DeliveryRouteRecord;
import com.da2jobu.delivery_route_recordservice.domain.vo.DeliveryRouteStatus;
import com.da2jobu.delivery_route_recordservice.domain.vo.RouteLocationType;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryRouteRecordDetailResponseDto(
        UUID routeRecordId,
        UUID deliveryId,
        Integer sequence,
        UUID originId,
        RouteLocationType originType,
        UUID destinationId,
        RouteLocationType destinationType,
        BigDecimal expectedDistanceKm,
        Integer expectedDurationMin,
        BigDecimal actualDistanceKm,
        Integer actualDurationMin,
        Integer remainDurationMin,
        DeliveryRouteStatus status,
        UUID deliveryManagerId
) {
    public static DeliveryRouteRecordDetailResponseDto from(DeliveryRouteRecord record) {
        return new DeliveryRouteRecordDetailResponseDto(
                record.getDeliveryRouteRecordId(),
                record.getDeliveryId(),
                record.getSequence(),
                record.getOriginId(),
                record.getOriginType(),
                record.getDestinationId(),
                record.getDestinationType(),
                record.getExpectedDistanceKm(),
                record.getExpectedDurationMin(),
                record.getRealDistanceKm(),
                record.getRealDurationMin(),
                record.getRemainDurationMin(),
                record.getStatus(),
                record.getDeliveryManagerId()
        );
    }
}
