package com.da2jobu.deliverymanagerservice.interfaces.dto.response;

import com.da2jobu.deliverymanagerservice.application.dto.result.DeliveryManagerResult;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;

import java.util.UUID;

public record DeliveryManagerResponse(
        UUID deliveryManagerId,
        UUID userId,
        UUID hubId,
        DeliveryManagerType type,
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