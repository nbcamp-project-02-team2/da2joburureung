package com.da2joburereung.userservice.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "내 정보 수정 요청")
public record UserMeUpdateRequest(

        @Schema(description = "이름", example = "홍길동", nullable = true)
        String name,

        @Schema(description = "이메일", example = "new@email.com", nullable = true)
        @Email
        String email,

        @Schema(description = "슬랙 ID", example = "U87654321", nullable = true)
        String slackId
) {
}