package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;

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
