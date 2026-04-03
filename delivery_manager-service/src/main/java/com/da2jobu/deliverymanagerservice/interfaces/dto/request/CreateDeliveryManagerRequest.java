package com.da2jobu.deliverymanagerservice.interfaces.dto.request;

import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDeliveryManagerRequest(
        @NotNull(message = "유저 ID는 필수입니다.")
        UUID userId,

        UUID hubId,

        @NotNull(message = "배송 매니저 타입은 필수입니다.")
        DeliveryManagerType type
) {
}