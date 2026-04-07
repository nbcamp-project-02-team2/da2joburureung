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
@Schema(description = "사용자 ID VO")
public class UserId {
    @Schema(description = "사용자 ID", example = "66666666-6666-6666-6666-666666666666")
    private UUID userId;

    public static UserId of(UUID userId) {
        return new UserId(userId);
    }

    private UserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유효하지 않은 사용자 id 입니다");
        }
        this.userId = userId;
    }
}