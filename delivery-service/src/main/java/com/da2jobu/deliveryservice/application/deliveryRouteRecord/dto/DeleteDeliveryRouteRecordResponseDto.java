package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

public record DeleteDeliveryRouteRecordResponseDto(
        String message
) {
    public static DeleteDeliveryRouteRecordResponseDto of(String message) {
        return new DeleteDeliveryRouteRecordResponseDto(message);
    }
}
