package com.da2jobu.deliveryservice.application.deliveryRouteRecord.service;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteMetricsCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.UpdateDeliveryRouteStatusCommand;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto.*;

import java.util.UUID;

public interface DeliveryRouteRecordService {

    CreateDeliveryRouteRecordsResponseDto createDeliveryRouteRecords(CreateDeliveryRouteRecordsCommand command);

    DeliveryRouteRecordDetailResponseDto getDeliveryRouteRecord(UUID routeRecordId,
                                                                UUID requesterId, String requesterRole);

    DeliveryRouteRecordListResponseDto getDeliveryRouteRecords(UUID deliveryId,
                                                               UUID requesterId, String requesterRole);

    UpdateDeliveryRouteStatusResponseDto updateDeliveryRouteStatus(
            UUID routeRecordId,
            UpdateDeliveryRouteStatusCommand command,
            UUID requesterId, String requesterRole
    );

    UpdateDeliveryRouteMetricsResponseDto updateDeliveryRouteMetrics(
            UUID routeRecordId,
            UpdateDeliveryRouteMetricsCommand command,
            UUID requesterId, String requesterRole
    );

    DeleteDeliveryRouteRecordResponseDto deleteDeliveryRouteRecord(UUID routeRecordId, String deletedBy,
                                                                   UUID requesterId, String requesterRole);
}