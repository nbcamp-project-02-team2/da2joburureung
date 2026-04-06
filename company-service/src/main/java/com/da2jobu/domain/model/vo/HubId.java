package com.da2jobu.domain.model.vo;

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

    @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222")
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

    public boolean isSameAs(UUID hubId) {
        return this.hubId.equals(hubId);
    }
}
