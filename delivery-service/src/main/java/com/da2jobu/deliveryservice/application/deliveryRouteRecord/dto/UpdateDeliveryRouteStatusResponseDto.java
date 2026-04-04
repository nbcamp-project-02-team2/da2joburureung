package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;

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
