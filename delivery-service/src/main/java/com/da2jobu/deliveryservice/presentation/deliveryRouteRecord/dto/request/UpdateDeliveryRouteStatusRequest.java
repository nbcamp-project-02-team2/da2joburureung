package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryRouteStatusRequest(
        @NotNull(message = "배송 경로 상태는 필수입니다.")
        DeliveryRouteStatus status
) {
    public UpdateDeliveryRouteStatusCommand toCommand() {
        return new UpdateDeliveryRouteStatusCommand(status);
    }
}
