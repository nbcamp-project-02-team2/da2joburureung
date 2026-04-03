package com.da2jobu.delivery_route_recordservice.application.dto;

public record DeleteDeliveryRouteRecordResponseDto(
        String message
) {
    public static DeleteDeliveryRouteRecordResponseDto of(String message) {
        return new DeleteDeliveryRouteRecordResponseDto(message);
    }
}
