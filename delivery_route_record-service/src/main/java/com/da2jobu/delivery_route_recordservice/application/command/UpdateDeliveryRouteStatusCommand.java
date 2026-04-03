package com.da2jobu.delivery_route_recordservice.application.command;

import com.da2jobu.delivery_route_recordservice.domain.vo.DeliveryRouteStatus;

public record UpdateDeliveryRouteStatusCommand(
        DeliveryRouteStatus status
) {
}
