package com.da2jobu.deliveryservice.presentation.delivery.dto.request;

import com.da2jobu.deliveryservice.application.delivery.command.UpdateDeliveryStatusCommand;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "배송 상태 변경 요청")
public record UpdateDeliveryStatusRequest(
        @Schema(description = "배송 상태", example = "IN_TRANSIT")
        @NotNull(message = "배송 상태는 필수입니다.")
        DeliveryStatus status
) {
    public UpdateDeliveryStatusCommand toCommand() {
        return new UpdateDeliveryStatusCommand(status);
    }
}
