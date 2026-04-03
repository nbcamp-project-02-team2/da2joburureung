package com.da2jobu.delivery_route_recordservice.application.service;

import com.da2jobu.delivery_route_recordservice.application.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.delivery_route_recordservice.application.command.UpdateDeliveryRouteMetricsCommand;
import com.da2jobu.delivery_route_recordservice.application.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.delivery_route_recordservice.application.dto.*;

import java.util.UUID;

public interface DeliveryRouteRecordService {

    CreateDeliveryRouteRecordsResponseDto createDeliveryRouteRecords(CreateDeliveryRouteRecordsCommand command);

    DeliveryRouteRecordDetailResponseDto getDeliveryRouteRecord(UUID routeRecordId);

    DeliveryRouteRecordListResponseDto getDeliveryRouteRecords(UUID deliveryId);

    UpdateDeliveryRouteStatusResponseDto updateDeliveryRouteStatus(
            UUID routeRecordId,
            UpdateDeliveryRouteStatusCommand command
    );

    UpdateDeliveryRouteMetricsResponseDto updateDeliveryRouteMetrics(
            UUID routeRecordId,
            UpdateDeliveryRouteMetricsCommand command
    );

    DeleteDeliveryRouteRecordResponseDto deleteDeliveryRouteRecord(UUID routeRecordId, String deletedBy);
}
