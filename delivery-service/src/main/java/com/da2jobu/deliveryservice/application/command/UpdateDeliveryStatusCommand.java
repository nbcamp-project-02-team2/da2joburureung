package com.da2jobu.deliveryservice.application.command;

import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;

public record UpdateDeliveryStatusCommand(
        DeliveryStatus status
) {
}
