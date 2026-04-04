package com.da2jobu.deliveryservice.presentation.deliveryManager.dto.request;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateDeliveryManagerRequest(
        UUID hubId,

        @NotNull(message = "배송 매니저 타입은 필수입니다.")
        DeliveryManagerType type
) {
}