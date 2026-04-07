package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "업체 배송 지점 정보")
public record CompanyDeliveryPoint(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryId,

        @Schema(description = "배송 경로 기록 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID deliveryRouteRecordId,

        @Schema(description = "도착지 업체 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID destinationId,

        @Schema(description = "허브로부터 거리(km)", example = "12.50")
        BigDecimal distanceFromHubKm,

        @Schema(description = "허브로부터 예상 소요 시간(분)", example = "35")
        int durationFromHubMin,

        @Schema(description = "요청 배송 시각", example = "2026-04-06T18:30:00")
        LocalDateTime requestedDeliveryTime,

        @Schema(description = "위도", example = "37.566500")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.978000")
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



