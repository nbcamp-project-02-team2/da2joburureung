package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "배송 생성 응답")
public record CreateDeliveryResponseDto(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryId,

        @Schema(description = "주문 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID orderId,

        @Schema(description = "배송 상태", example = "READY")
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
