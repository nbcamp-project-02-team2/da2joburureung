package com.da2jobu.deliveryservice.application.delivery.command;

import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 상태 변경 커맨드")
public record UpdateDeliveryStatusCommand(
        @Schema(description = "변경할 배송 상태", example = "OUT_FOR_DELIVERY")
        DeliveryStatus status
) {
}