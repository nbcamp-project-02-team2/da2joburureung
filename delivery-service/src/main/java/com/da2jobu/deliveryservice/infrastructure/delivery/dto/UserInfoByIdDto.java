package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "사용자 ID 기반 정보 응답")
public record UserInfoByIdDto(
        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,
        String username,
        String name,
        String email,
        String slackId,

        @Schema(description = "사용자 역할", example = "COMPANY_DELIVERY")
        String userRole,
        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,
        UUID companyId
) {
}
