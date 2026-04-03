package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;

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
