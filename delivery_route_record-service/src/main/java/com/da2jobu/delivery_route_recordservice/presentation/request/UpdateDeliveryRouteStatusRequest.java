package com.da2jobu.delivery_route_recordservice.presentation.request;

import com.da2jobu.delivery_route_recordservice.application.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.delivery_route_recordservice.domain.vo.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryRouteStatusRequest(
        @NotNull(message = "배송 경로 상태는 필수입니다.")
        DeliveryRouteStatus status
) {
    public UpdateDeliveryRouteStatusCommand toCommand() {
        return new UpdateDeliveryRouteStatusCommand(status);
    }
}
