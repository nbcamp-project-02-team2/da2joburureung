package com.da2jobu.deliveryservice.application.deliveryRouteRecord.command;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 경로 상태 변경 커맨드")
public record UpdateDeliveryRouteStatusCommand(
        @Schema(description = "변경할 배송 경로 상태", example = "OUT_FOR_DELIVERY")
        DeliveryRouteStatus status
) {
}
