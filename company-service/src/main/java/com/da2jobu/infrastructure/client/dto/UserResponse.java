package com.da2jobu.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "유저 서비스 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        UUID userId,

        @Schema(description = "사용자 역할", example = "MASTER")
        String userRole,

        @Schema(description = "소속 허브 ID", example = "11111111-1111-1111-1111-111111111111", nullable = true)
        UUID hubId,

        @Schema(description = "소속 업체 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID companyId
) {
}