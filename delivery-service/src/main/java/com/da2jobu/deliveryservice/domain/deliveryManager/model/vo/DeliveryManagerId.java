package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "배송 담당자 ID VO")
public class DeliveryManagerId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "배송 담당자 ID", example = "33333333-3333-3333-3333-333333333333")
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