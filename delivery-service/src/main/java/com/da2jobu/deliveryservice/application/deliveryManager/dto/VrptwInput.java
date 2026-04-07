package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "VRPTW 최적화 입력 데이터")
public record VrptwInput(
        @Schema(description = "배송 지점 목록")
        List<CompanyDeliveryPoint> deliveryPoints,

        @Schema(description = "사용 가능한 배송 담당자 수", example = "3")
        int vehicleCount,

        @Schema(description = "최대 배송 시간(분)", example = "480")
        long maxRouteTimeMinutes,

        @Schema(description = "배차 시작 시각", example = "2026-04-06T09:00:00")
        LocalDateTime batchStartTime
) {
    public static VrptwInput of(List<CompanyDeliveryPoint> points, int vehicleCount,
                                LocalDateTime batchStartTime) {
        return new VrptwInput(points, vehicleCount, 480, batchStartTime);
    }
}