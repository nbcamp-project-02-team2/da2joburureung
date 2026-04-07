package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "배송 경로 생성 응답")
public record CreateDeliveryRouteRecordsResponseDto(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryId,

        @Schema(description = "생성된 경로 개수", example = "3")
        Integer routeCount,

        @Schema(description = "생성된 배송 경로 정보 목록")
        List<RouteRecordInfo> routes
) {
    public static CreateDeliveryRouteRecordsResponseDto of(UUID deliveryId, List<RouteRecordInfo> routes) {
        return new CreateDeliveryRouteRecordsResponseDto(deliveryId, routes.size(), routes);
    }

    @Schema(description = "생성된 배송 경로 정보")
    public record RouteRecordInfo(
            @Schema(description = "경로 기록 ID", example = "22222222-2222-2222-2222-222222222222")
            UUID routeRecordId,

            @Schema(description = "경로 순번", example = "1")
            Integer sequence
    ) {
    }
}
