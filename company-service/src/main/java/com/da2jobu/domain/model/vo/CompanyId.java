package com.da2jobu.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID companyId;

    public static CompanyId of() {
        return CompanyId.of(UUID.randomUUID());
    }
    public static CompanyId of(UUID companyId) {
        return new CompanyId(companyId);
    }

    private CompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("유효하지 않은 업체 id 입니다");
        }
        this.companyId = companyId;
    }

    public boolean isSameAs(UUID companyId) {
        return this.companyId.equals(companyId);
    }
}
