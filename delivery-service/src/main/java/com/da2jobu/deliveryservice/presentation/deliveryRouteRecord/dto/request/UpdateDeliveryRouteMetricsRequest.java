package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteMetricsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "배송 경로 실적 갱신 요청")
public record UpdateDeliveryRouteMetricsRequest(
        @Schema(description = "실제 이동 거리(km)", example = "10.8")
        @NotNull(message = "실제 이동 거리는 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true, message = "실제 이동 거리는 0 이상이어야 합니다.")
        BigDecimal actualDistanceKm,

        @Schema(description = "실제 소요 시간(분)", example = "40")
        @NotNull(message = "실제 소요 시간은 필수입니다.")
        @Min(value = 0, message = "실제 소요 시간은 0 이상이어야 합니다.")
        Integer actualDurationMin,

        @Schema(description = "남은 총 예상 시간(분)", example = "50")
        @NotNull(message = "남은 총 예상 시간은 필수입니다.")
        @Min(value = 0, message = "남은 총 예상 시간은 0 이상이어야 합니다.")
        Integer remainDurationMin
) {
    public UpdateDeliveryRouteMetricsCommand toCommand() {
        return new UpdateDeliveryRouteMetricsCommand(
                actualDistanceKm,
                actualDurationMin,
                remainDurationMin
        );
    }
}
