package com.da2jobu.delivery_route_recordservice.application.dto;


import com.da2jobu.delivery_route_recordservice.domain.entity.DeliveryRouteRecord;
import com.da2jobu.delivery_route_recordservice.domain.vo.DeliveryRouteStatus;

import java.util.UUID;

public record UpdateDeliveryRouteStatusResponseDto(
        UUID routeRecordId,
        DeliveryRouteStatus status
) {
    public static UpdateDeliveryRouteStatusResponseDto from(DeliveryRouteRecord record) {
        return new UpdateDeliveryRouteStatusResponseDto(
                record.getDeliveryRouteRecordId(),
                record.getStatus()
        );
    }
}
