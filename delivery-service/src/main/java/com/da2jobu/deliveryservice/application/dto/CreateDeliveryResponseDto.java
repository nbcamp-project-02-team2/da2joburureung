package com.da2jobu.deliveryservice.application.dto;

import com.da2jobu.deliveryservice.domain.entity.Delivery;
import com.da2jobu.deliveryservice.domain.vo.DeliveryStatus;

import java.util.UUID;

public record CreateDeliveryResponseDto(
        UUID deliveryId,
        UUID orderId,
        DeliveryStatus status
) {
    public static CreateDeliveryResponseDto from(Delivery delivery) {
        return new CreateDeliveryResponseDto(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getStatus()
        );
    }
}
