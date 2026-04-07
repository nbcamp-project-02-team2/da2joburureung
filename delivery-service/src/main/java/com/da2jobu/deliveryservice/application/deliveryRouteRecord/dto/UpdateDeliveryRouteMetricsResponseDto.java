package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "배송 경로 실적 갱신 응답")
public record UpdateDeliveryRouteMetricsResponseDto(
        @Schema(description = "경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID routeRecordId,

        @Schema(description = "실제 이동 거리(km)", example = "10.8")
        BigDecimal actualDistanceKm,

        @Schema(description = "실제 소요 시간(분)", example = "40")
        Integer actualDurationMin,

        @Schema(description = "남은 총 예상 시간(분)", example = "50")
        Integer remainDurationMin
) {
    public static UpdateDeliveryRouteMetricsResponseDto from(DeliveryRouteRecord record) {
        return new UpdateDeliveryRouteMetricsResponseDto(
                record.getDeliveryRouteRecordId(),
                record.getRealDistanceKm(),
                record.getRealDurationMin(),
                record.getRemainDurationMin()
        );
    }
}
