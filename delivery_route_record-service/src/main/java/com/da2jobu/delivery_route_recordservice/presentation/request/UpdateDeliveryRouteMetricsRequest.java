package com.da2jobu.delivery_route_recordservice.presentation.request;

import com.da2jobu.delivery_route_recordservice.application.command.UpdateDeliveryRouteMetricsCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDeliveryRouteMetricsRequest(
        @NotNull(message = "실제 이동 거리는 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = true, message = "실제 이동 거리는 0 이상이어야 합니다.")
        BigDecimal actualDistanceKm,

        @NotNull(message = "실제 소요 시간은 필수입니다.")
        @Min(value = 0, message = "실제 소요 시간은 0 이상이어야 합니다.")
        Integer actualDurationMin,

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
