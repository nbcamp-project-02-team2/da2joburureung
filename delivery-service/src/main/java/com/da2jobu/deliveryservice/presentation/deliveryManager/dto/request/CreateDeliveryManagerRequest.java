package com.da2jobu.deliveryservice.presentation.deliveryManager.dto.request;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "배송 담당자 생성 요청")
public record CreateDeliveryManagerRequest(
        @Schema(description = "유저 ID", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,

        @Schema(description = "배송 담당자 타입", example = "HUB_DELIVERY")
        @NotNull(message = "배송 매니저 타입은 필수입니다.")
        DeliveryManagerType type
) {
}