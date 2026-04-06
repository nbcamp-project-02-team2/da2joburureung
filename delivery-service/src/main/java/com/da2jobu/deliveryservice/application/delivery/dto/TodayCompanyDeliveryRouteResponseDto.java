package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "오늘의 업체 배송 경로 응답")
public record TodayCompanyDeliveryRouteResponseDto(

        @Schema(description = "배송 경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryRouteRecordId,

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
        BigDecimal realDistanceKm,

        @Schema(description = "실제 소요 시간(분)", example = "40", nullable = true)
        Integer realDurationMin,

        @Schema(description = "남은 총 예상 시간(분)", example = "50")
        Integer remainDurationMin,

        @Schema(description = "배송 경로 상태", example = "IN_PROGRESS")
        DeliveryRouteStatus status,

        @Schema(description = "배송 담당자 ID", example = "55555555-5555-5555-5555-555555555555")
        UUID deliveryManagerId,

        @Schema(description = "희망 배송 시각", example = "2026-04-06T18:30:00")
        LocalDateTime desiredDeliveryAt
) {
}
