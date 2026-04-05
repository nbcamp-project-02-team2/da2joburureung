package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyDeliveryPoint(
        UUID deliveryId,
        UUID deliveryRouteRecordId,
        UUID destinationId,
        BigDecimal distanceFromHubKm,
        int durationFromHubMin,
        LocalDateTime requestedDeliveryTime,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static CompanyDeliveryPoint of(UUID deliveryId, UUID deliveryRouteRecordId, UUID destinationId,
                                          BigDecimal distanceFromHubKm, int durationFromHubMin,
                                          LocalDateTime requestedDeliveryTime,
                                          BigDecimal latitude, BigDecimal longitude) {
        return new CompanyDeliveryPoint(deliveryId, deliveryRouteRecordId, destinationId,
                distanceFromHubKm, durationFromHubMin, requestedDeliveryTime, latitude, longitude);
    }

}



