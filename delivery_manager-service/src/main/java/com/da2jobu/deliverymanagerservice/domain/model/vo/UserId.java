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
public class UserId {
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