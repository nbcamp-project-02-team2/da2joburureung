package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "사용자 역할", example = "COMPANY_DELIVERY")
        String role
) {
}