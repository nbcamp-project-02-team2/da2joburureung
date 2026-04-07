package com.da2jobu.deliveryservice.application.delivery.command;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "주문 승인 이벤트 기반 배송 생성 커맨드")
public record CreateDeliveryFromOrderCommand(
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
}