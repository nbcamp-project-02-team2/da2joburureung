package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "로그인 응답")
public record LoginResponse(

        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "아이디", example = "master01")
        String username,

        @Schema(description = "사용자 권한", example = "MASTER")
        UserRole role,

        @Schema(description = "사용자 상태", example = "APPROVED")
        UserStatus status
) {
}