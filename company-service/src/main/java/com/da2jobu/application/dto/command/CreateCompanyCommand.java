package com.da2jobu.application.dto.command;

import com.da2jobu.domain.model.vo.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "업체 생성 커맨드")
public record CreateCompanyCommand(
        @Schema(description = "요청 사용자 역할", example = "MASTER")
        String userRole,

        @Schema(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,

        @Schema(description = "관리 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID hubId,

        @Schema(description = "업체명", example = "서울상사")
        String name,

        @Schema(description = "업체 타입", example = "PRODUCER")
        CompanyType type,

        @Schema(description = "업체 주소", example = "서울특별시 강남구 테헤란로 123")
        String address
) {
}