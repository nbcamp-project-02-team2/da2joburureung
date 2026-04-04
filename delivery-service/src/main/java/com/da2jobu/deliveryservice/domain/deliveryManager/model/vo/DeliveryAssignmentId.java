package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

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
public class DeliveryAssignmentId {
    private UUID deliveryAssignmentId;

    public static DeliveryAssignmentId of() {
        return DeliveryAssignmentId.of(UUID.randomUUID());
    }

    public static DeliveryAssignmentId of(UUID deliveryAssignmentId) {
        return new DeliveryAssignmentId(deliveryAssignmentId);
    }

    private DeliveryAssignmentId(UUID deliveryAssignmentId) {
        if (deliveryAssignmentId == null) {
            throw new IllegalArgumentException("유효하지 않은 배송 담당자 배정 이력 id 입니다");
        }
        this.deliveryAssignmentId = deliveryAssignmentId;
    }
}