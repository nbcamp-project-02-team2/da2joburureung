package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TodayCompanyDeliveryRouteResponseDto(
        UUID deliveryRouteRecordId,
        UUID deliveryId,
        Integer sequence,
        UUID originId,
        RouteLocationType originType,
        UUID destinationId,
        RouteLocationType destinationType,
        BigDecimal expectedDistanceKm,
        Integer expectedDurationMin,
        BigDecimal realDistanceKm,
        Integer realDurationMin,
        Integer remainDurationMin,
        DeliveryRouteStatus status,
        UUID deliveryManagerId,
        LocalDateTime desiredDeliveryAt
) {
}
