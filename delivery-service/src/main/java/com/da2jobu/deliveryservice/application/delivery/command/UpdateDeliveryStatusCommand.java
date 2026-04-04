package com.da2jobu.deliveryservice.application.delivery.command;

import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;

public record UpdateDeliveryStatusCommand(
        DeliveryStatus status
) {
}
