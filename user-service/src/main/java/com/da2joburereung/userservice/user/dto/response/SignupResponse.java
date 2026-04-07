package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "회원가입 응답")
public record SignupResponse(

        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "아이디", example = "master01")
        String username,

        @Schema(description = "가입 상태", example = "PENDING")
        UserStatus status
) {
}