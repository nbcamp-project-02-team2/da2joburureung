package com.da2jobu.deliverymanagerservice.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryId {
    private UUID deliveryId;

    public static DeliveryId of(UUID deliveryId) {
        return new DeliveryId(deliveryId);
    }

    private DeliveryId(UUID deliveryId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("유효하지 않은 배송 id 입니다");
        }
        this.deliveryId = deliveryId;
    }
}