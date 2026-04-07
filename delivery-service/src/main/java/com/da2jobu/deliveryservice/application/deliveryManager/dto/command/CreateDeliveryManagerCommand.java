package com.da2jobu.deliveryservice.application.deliveryManager.dto.command;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "배송 담당자 생성 커맨드")
public record CreateDeliveryManagerCommand(
        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,

        @Schema(description = "배송 담당자 타입", example = "HUB_DELIVERY")
        DeliveryManagerType type,

        @Schema(description = "요청자 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID requesterId,

        @Schema(description = "요청자 역할", example = "MASTER")
        String requesterRole
) {
}
