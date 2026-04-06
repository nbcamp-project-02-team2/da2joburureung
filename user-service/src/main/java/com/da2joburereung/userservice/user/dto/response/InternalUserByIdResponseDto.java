package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "내부 사용자 단건 조회 응답")
public record InternalUserByIdResponseDto(
        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "사용자 권한", example = "HUB_MANAGER")
        UserRole userRole,

        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,

        @Schema(description = "업체 ID", example = "33333333-3333-3333-3333-333333333333", nullable = true)
        UUID companyId
) {
}