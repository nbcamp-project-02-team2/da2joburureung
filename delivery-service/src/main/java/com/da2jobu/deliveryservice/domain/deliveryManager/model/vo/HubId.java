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
public class HubId {
    private UUID hubId;

    public static HubId of(UUID hubId) {
        if (hubId == null) return null;
        return new HubId(hubId);
    }

    private HubId(UUID hubId) {
        this.hubId = hubId;
    }

    public boolean isSameAs(UUID hubId) {
        return this.hubId.equals(hubId);
    }
}