package com.da2jobu.interfaces.dto.request;

import com.da2jobu.domain.model.vo.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "업체 생성 요청")
public record CreateCompanyRequest(

        @Schema(description = "업체명", example = "서울상사")
        @NotBlank(message = "업체명은 필수입니다.")
        @Size(max = 255, message = "업체명은 255자 이하여야 합니다.")
        String name,

        @Schema(description = "업체 타입", example = "PRODUCER")
        @NotNull(message = "업체 타입은 필수입니다.")
        CompanyType type,

        @Schema(description = "관리 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "관리 허브 ID는 필수입니다.")
        UUID hubId,

        @Schema(description = "업체 주소", example = "서울특별시 강남구 테헤란로 123")
        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다.")
        String address
) {
}