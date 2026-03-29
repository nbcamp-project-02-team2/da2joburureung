package com.da2jobu.domain.model.vo;

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
public class ManagerId {
    private UUID managerId;

    public static ManagerId of(UUID managerId) {
        return new ManagerId(managerId);
    }

    private ManagerId(UUID managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("유효하지 않은 담당자 id 입니다");
        }
    }
}
