package com.da2jobu.deliveryservice.application.deliveryRouteRecord.command;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "배송 경로 실적 갱신 커맨드")
public record UpdateDeliveryRouteMetricsCommand(
        @Schema(description = "실제 이동 거리(km)", example = "10.80")
        BigDecimal actualDistanceKm,

        @Schema(description = "실제 소요 시간(분)", example = "40")
        Integer actualDurationMin,

        @Schema(description = "남은 총 예상 시간(분)", example = "20")
        Integer remainDurationMin
) {
}