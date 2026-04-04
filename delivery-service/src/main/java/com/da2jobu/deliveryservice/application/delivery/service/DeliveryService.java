package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.UpdateDeliveryStatusCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryDetailResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryListResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;

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
