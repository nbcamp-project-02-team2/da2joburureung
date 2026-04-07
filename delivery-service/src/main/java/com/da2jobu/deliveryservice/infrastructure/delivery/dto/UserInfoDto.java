package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 정보 DTO")
public record UserInfoDto(
        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "사용자명", example = "honggildong")
        String username,

        @Schema(description = "이름", example = "홍길동")
        String name,
        String email,

        @Schema(description = "슬랙 ID", example = "U12345678")
        String slackId,

        @Schema(description = "역할", example = "COMPANY_DELIVERY")
        String role
) {
}
