package com.da2jobu.presentation.dto.request;

import com.da2jobu.domain.model.vo.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCompanyRequest(

        @NotNull(message = "업체 담당자 ID는 필수입니다.")
        UUID managerId,

        @NotNull(message = "관리 허브 ID는 필수입니다.")
        UUID hubId,

        @NotBlank(message = "업체명은 필수입니다.")
        @Size(max = 255, message = "업체명은 255자 이하여야 합니다.")
        String name,

        @NotNull(message = "업체 타입은 필수입니다.")
        CompanyType type,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다.")
        String address
) {
}