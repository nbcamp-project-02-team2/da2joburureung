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
@Schema(description = "허브 ID VO")
public class HubId {
    @Schema(description = "허브 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
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