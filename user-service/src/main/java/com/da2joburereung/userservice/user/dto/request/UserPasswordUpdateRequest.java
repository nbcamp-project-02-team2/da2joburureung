package com.da2joburereung.userservice.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record UserPasswordUpdateRequest(

        @Schema(description = "현재 비밀번호", example = "oldPassword123!")
        @NotBlank
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "newPassword123!")
        @NotBlank
        String newPassword
) {
}