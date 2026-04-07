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
@Schema(description = "배송 경로 기록 ID VO")
public class DeliveryRouteRecordId {

    @Schema(description = "배송 경로 기록 ID", example = "44444444-4444-4444-4444-444444444444")
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