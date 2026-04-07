package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "배송 경로 목록 응답")
public record DeliveryRouteRecordListResponseDto(
        @Schema(description = "배송 경로 목록")
        List<DeliveryRouteRecordSummaryResponseDto> routes
) {
    public static DeliveryRouteRecordListResponseDto from(List<DeliveryRouteRecordSummaryResponseDto> routes) {
        return new DeliveryRouteRecordListResponseDto(routes);
    }
}
