package com.da2jobu.deliveryservice.application.deliveryRouteRecord.service;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteMetricsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto.*;

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
