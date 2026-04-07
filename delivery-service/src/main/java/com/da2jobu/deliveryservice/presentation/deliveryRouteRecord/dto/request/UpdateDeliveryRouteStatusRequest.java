package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "배송 경로 상태 변경 요청")
public record UpdateDeliveryRouteStatusRequest(
        @Schema(description = "배송 경로 상태", example = "OUT_FOR_DELIVERY")
        @NotNull(message = "배송 경로 상태는 필수입니다.")
        DeliveryRouteStatus status
) {
    public UpdateDeliveryRouteStatusCommand toCommand() {
        return new UpdateDeliveryRouteStatusCommand(status);
    }
}
