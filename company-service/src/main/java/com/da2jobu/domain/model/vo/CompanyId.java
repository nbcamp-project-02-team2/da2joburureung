package com.da2jobu.domain.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "업체 ID VO")
public class CompanyId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "업체 ID", example = "11111111-1111-1111-1111-111111111111")
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
