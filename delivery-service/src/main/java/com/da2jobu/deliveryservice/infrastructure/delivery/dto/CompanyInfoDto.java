package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "업체 정보 DTO")
public record CompanyInfoDto(
        @Schema(description = "업체 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID companyId,

        @Schema(description = "소속 허브 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID hubId,

        @Schema(description = "업체 주소", example = "서울특별시 강남구 테헤란로 123")
        String address,

        @Schema(description = "위도", example = "37.566500")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.978000")
        BigDecimal longitude
) {
}
