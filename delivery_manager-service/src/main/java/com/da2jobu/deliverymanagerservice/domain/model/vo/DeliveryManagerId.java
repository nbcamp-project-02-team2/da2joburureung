package com.da2jobu.deliverymanagerservice.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManagerId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID deliveryManagerId;

    public static DeliveryManagerId of() {
        return DeliveryManagerId.of(UUID.randomUUID());
    }

    public static DeliveryManagerId of(UUID deliveryManagerId) {
        return new DeliveryManagerId(deliveryManagerId);
    }

    private DeliveryManagerId(UUID deliveryManagerId) {
        if (deliveryManagerId == null) {
            throw new IllegalArgumentException("유효하지 않은 배송담당자 id 입니다");
        }
        this.deliveryManagerId = deliveryManagerId;
    }
}