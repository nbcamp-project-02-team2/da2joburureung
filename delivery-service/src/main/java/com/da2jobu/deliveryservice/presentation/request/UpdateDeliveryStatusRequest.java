package com.da2jobu.deliveryservice.presentation.request;

import com.da2jobu.deliveryservice.application.command.UpdateDeliveryStatusCommand;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusRequest(
        @NotNull(message = "배송 상태는 필수입니다.")
        DeliveryStatus status
) {
    public UpdateDeliveryStatusCommand toCommand() {
        return new UpdateDeliveryStatusCommand(status);
    }
}
