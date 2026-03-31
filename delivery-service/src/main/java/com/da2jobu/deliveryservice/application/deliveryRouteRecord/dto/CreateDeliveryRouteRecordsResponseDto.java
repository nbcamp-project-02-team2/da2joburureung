package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import java.util.List;
import java.util.UUID;

public record CreateDeliveryRouteRecordsResponseDto(
        UUID deliveryId,
        Integer routeCount,
        List<RouteRecordInfo> routes
) {
    public static CreateDeliveryRouteRecordsResponseDto of(UUID deliveryId, List<RouteRecordInfo> routes) {
        return new CreateDeliveryRouteRecordsResponseDto(deliveryId, routes.size(), routes);
    }

    public record RouteRecordInfo(UUID routeRecordId, Integer sequence) {
    }
}
