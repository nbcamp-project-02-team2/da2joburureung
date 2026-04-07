package com.da2jobu.deliveryservice.presentation.deliveryManager.dto.response;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryManagerResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "배송 담당자 응답")
public record DeliveryManagerResponse(
        @Schema(description = "배송 담당자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryManagerId,
        @Schema(description = "유저 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID userId,
        @Schema(description = "허브 ID", example = "33333333-3333-3333-3333-333333333333", nullable = true)
        UUID hubId,
        @Schema(description = "배송 담당자 타입", example = "HUB_DELIVERY")
        DeliveryManagerType type,
        @Schema(description = "순번", example = "1")
        int seq
) {
    public static DeliveryManagerResponse from(DeliveryManagerResult result) {
        return new DeliveryManagerResponse(
                result.deliveryManagerId(),
                result.userId(),
                result.hubId(),
                result.type(),
                result.seq()
        );
    }
}