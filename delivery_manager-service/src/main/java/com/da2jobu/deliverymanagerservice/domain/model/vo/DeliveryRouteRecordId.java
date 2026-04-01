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
public class DeliveryRouteRecordId {
    private UUID deliveryRouteRecordId;

    public static DeliveryRouteRecordId of(UUID deliveryRouteRecordId) {
        return new DeliveryRouteRecordId(deliveryRouteRecordId);
    }

    private DeliveryRouteRecordId(UUID deliveryRouteRecordId) {
        if (deliveryRouteRecordId == null) {
            throw new IllegalArgumentException("유효하지 않은 배송 경로 기록 id 입니다");
        }
        this.deliveryRouteRecordId = deliveryRouteRecordId;
    }
}