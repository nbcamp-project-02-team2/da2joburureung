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
public class HubId {
    private UUID hubId;

    public static HubId of(UUID hubId) {
        return new HubId(hubId);
    }

    private HubId(UUID hubId) {
        if (hubId == null) {
            throw new IllegalArgumentException("유효하지 않은 허브 id 입니다");
        }
        this.hubId = hubId;
    }
}