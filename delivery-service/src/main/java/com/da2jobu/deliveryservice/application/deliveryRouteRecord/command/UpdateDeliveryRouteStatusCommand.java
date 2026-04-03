package com.da2jobu.deliveryservice.application.deliveryRouteRecord.command;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;

public record UpdateDeliveryRouteStatusCommand(
        DeliveryRouteStatus status
) {
}
