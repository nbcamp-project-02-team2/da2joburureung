package com.da2jobu.deliveryservice.application.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Schema(description = "주문 승인 이벤트")
public record OrderAcceptedEvent(
        @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID orderId,

        @Schema(description = "공급업체 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID supplierId,

        @Schema(description = "수령인 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID receiverId,

        @Schema(description = "요청사항", example = "문 앞에 놓아주세요.", nullable = true)
        String requirements,

        @Schema(description = "생성자", example = "system")
        String createdBy,

        @Schema(description = "희망 배송 시각", example = "2026-04-06T18:30:00")
        LocalDateTime desiredDeliveryAt
) {
    public OrderAcceptedEvent {
        Objects.requireNonNull(orderId, "orderId는 null이면 안됩니다.");
        Objects.requireNonNull(supplierId, "supplierId는 null이면 안됩니다.");
        Objects.requireNonNull(receiverId, "receiverId는 null이면 안됩니다.");
        Objects.requireNonNull(desiredDeliveryAt, "desiredDeliveryAt은 null이면 안됩니다.");

        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy는 blank면 안됩니다.");
        }
    }
}