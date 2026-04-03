package com.da2jobu.deliverymanagerservice.application.dto.result;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;

import java.util.UUID;

public record DeliveryManagerResult(
        UUID deliveryManagerId,
        UUID userId,
        UUID hubId,
        DeliveryManagerType type,
        int seq
) {
    public static DeliveryManagerResult from(DeliveryManager deliveryManager) {
        return new DeliveryManagerResult(
                deliveryManager.getDeliveryManagerId().getDeliveryManagerId(),
                deliveryManager.getUserId().getUserId(),
                deliveryManager.getHubId() != null ? deliveryManager.getHubId().getHubId() : null,
                deliveryManager.getType(),
                deliveryManager.getSeq()
        );
    }
}
