package com.da2jobu.delivery_route_recordservice.application.dto;

import java.util.List;

public record DeliveryRouteRecordListResponseDto(
        List<DeliveryRouteRecordSummaryResponseDto> routes
) {
    public static DeliveryRouteRecordListResponseDto from(List<DeliveryRouteRecordSummaryResponseDto> routes) {
        return new DeliveryRouteRecordListResponseDto(routes);
    }
}
