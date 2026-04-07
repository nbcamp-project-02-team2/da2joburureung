package com.da2jobu.application.dto.result;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "업체 조회 결과")
public record CompanyResult(
        @Schema(description = "업체 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID companyId,

        @Schema(description = "관리 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID hubId,

        @Schema(description = "업체명", example = "서울상사")
        String name,

        @Schema(description = "업체 타입", example = "PRODUCER")
        CompanyType type,

        @Schema(description = "업체 주소", example = "서울특별시 강남구 테헤란로 123")
        String address,

        @Schema(description = "위도", example = "37.498095")
        BigDecimal latitude,

        @Schema(description = "경도", example = "127.027610")
        BigDecimal longitude
) {
    public static CompanyResult from(Company company) {
        return new CompanyResult(
                company.getCompanyId().getCompanyId(),
                company.getHubId().getHubId(),
                company.getName(),
                company.getType(),
                company.getLocation().getAddress(),
                company.getLocation().getLatitude(),
                company.getLocation().getLongitude()
        );
    }
}
