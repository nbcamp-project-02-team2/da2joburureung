package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "배송 경로 상세 응답")
public record DeliveryRouteRecordDetailResponseDto(
        @Schema(description = "경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID routeRecordId,

        @Schema(description = "배송 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID deliveryId,

        @Schema(description = "경로 순번", example = "1")
        Integer sequence,

        @Schema(description = "출발지 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID originId,

        @Schema(description = "출발지 타입", example = "HUB")
        RouteLocationType originType,

        @Schema(description = "도착지 ID", example = "44444444-4444-4444-4444-444444444444")
        UUID destinationId,

        @Schema(description = "도착지 타입", example = "COMPANY")
        RouteLocationType destinationType,

        @Schema(description = "예상 거리(km)", example = "12.5")
        BigDecimal expectedDistanceKm,

        @Schema(description = "예상 소요 시간(분)", example = "45")
        Integer expectedDurationMin,

        @Schema(description = "실제 이동 거리(km)", example = "10.8", nullable = true)
        BigDecimal actualDistanceKm,

        @Schema(description = "실제 소요 시간(분)", example = "40", nullable = true)
        Integer actualDurationMin,

        @Schema(description = "남은 총 예상 시간(분)", example = "50")
        Integer remainDurationMin,

        @Schema(description = "배송 경로 상태", example = "WAITING")
        DeliveryRouteStatus status,

        @Schema(description = "배송 담당자 ID", example = "55555555-5555-5555-5555-555555555555")
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
