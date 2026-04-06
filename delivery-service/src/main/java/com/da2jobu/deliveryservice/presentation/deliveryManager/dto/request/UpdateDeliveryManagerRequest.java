package com.da2jobu.deliveryservice.presentation.deliveryManager.dto.request;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "배송 담당자 수정 요청")
public record UpdateDeliveryManagerRequest(
        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,

        @Schema(description = "배송 담당자 타입", example = "COMPANY_DELIVERY_MANAGER")
        @NotNull(message = "배송 매니저 타입은 필수입니다.")
        DeliveryManagerType type
) {
}