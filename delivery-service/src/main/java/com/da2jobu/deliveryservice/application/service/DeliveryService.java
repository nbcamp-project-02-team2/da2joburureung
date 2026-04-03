package com.da2jobu.deliveryservice.application.service;

import com.da2jobu.deliveryservice.application.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.command.UpdateDeliveryStatusCommand;
import com.da2jobu.deliveryservice.application.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.dto.DeliveryDetailResponseDto;
import com.da2jobu.deliveryservice.application.dto.DeliveryListResponseDto;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;

import java.util.UUID;

public interface DeliveryService {
    CreateDeliveryResponseDto createDelivery(CreateDeliveryCommand command);

    DeliveryDetailResponseDto getDelivery(UUID deliveryId);

    DeliveryListResponseDto getDeliveries(
            UUID orderId,
            DeliveryStatus status,
            UUID originHubId,
            UUID destinationHubId,
            int page,
            int size
    );

    DeliveryDetailResponseDto updateDeliveryStatus(UUID deliveryId, UpdateDeliveryStatusCommand command);

    void deleteDelivery(UUID deliveryId, String deletedBy);
}
