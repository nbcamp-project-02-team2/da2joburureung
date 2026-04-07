package com.da2jobu.deliveryservice.application.deliveryRouteRecord.command;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "배송 경로 일괄 생성 커맨드")
public record CreateDeliveryRouteRecordsCommand(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryId,

        @Schema(description = "생성할 경로 목록")
        List<RouteItem> routes
) {
    @Schema(description = "배송 경로 항목")
    public record RouteItem(
            @Schema(description = "경로 순번", example = "1")
            Integer sequence,

            @Schema(description = "출발지 ID", example = "22222222-2222-2222-2222-222222222222")
            UUID originId,

            @Schema(description = "출발지 타입", example = "HUB")
            RouteLocationType originType,

            @Schema(description = "도착지 ID", example = "33333333-3333-3333-3333-333333333333")
            UUID destinationId,

            @Schema(description = "도착지 타입", example = "COMPANY")
            RouteLocationType destinationType,

            @Schema(description = "예상 거리(km)", example = "12.50")
            BigDecimal expectedDistanceKm,

            @Schema(description = "예상 소요 시간(분)", example = "45")
            Integer expectedDurationMin,

            @Schema(description = "배송 담당자 ID", example = "44444444-4444-4444-4444-444444444444", nullable = true)
            UUID deliveryManagerId,

            @Schema(description = "남은 총 예상 시간(분)", example = "60")
            Integer remainDurationMin
    ) {
    }
}
