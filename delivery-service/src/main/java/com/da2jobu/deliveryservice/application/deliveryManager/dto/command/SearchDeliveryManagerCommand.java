package com.da2jobu.deliveryservice.application.deliveryManager.dto.command;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "배송 담당자 조회 커맨드")
public record SearchDeliveryManagerCommand(
        @Schema(description = "배송 담당자 타입", example = "HUB_DELIVERY", nullable = true)
        DeliveryManagerType type,

        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111", nullable = true)
        UUID hubId,

        @Schema(description = "페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "요청자 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID requesterId,

        @Schema(description = "요청자 역할", example = "MASTER")
        String requesterRole
) {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_SIZE = 10;

    public int validatedSize() {
        return ALLOWED_SIZES.contains(size) ? size : DEFAULT_SIZE;
    }

    public int validatedPage() {
        return Math.max(page, 0);
    }
}