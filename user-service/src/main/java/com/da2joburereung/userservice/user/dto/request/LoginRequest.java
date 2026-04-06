package com.da2joburereung.userservice.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "아이디", example = "master01")
        @NotBlank String username,

        @Schema(description = "비밀번호", example = "Password1!")
        @NotBlank String password
) {
}