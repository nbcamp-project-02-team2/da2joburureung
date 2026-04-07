package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "배송 ID VO")
public class DeliveryId {

    @Schema(description = "배송 ID", example = "22222222-2222-2222-2222-222222222222")
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