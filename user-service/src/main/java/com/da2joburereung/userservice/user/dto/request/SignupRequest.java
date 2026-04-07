package com.da2joburereung.userservice.user.dto.request;

import com.da2joburereung.userservice.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "아이디", example = "master01")
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]{4,10}$")
        String username,

        @Schema(description = "비밀번호", example = "Password1!")
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,15}$"
        )
        String password,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "이메일", example = "test@email.com")
        @NotBlank
        String email,

        @Schema(description = "슬랙 ID", example = "U12345678")
        @NotBlank
        String slackId,

        @Schema(description = "사용자 역할", example = "MASTER")
        @NotNull
        UserRole role,

        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111", nullable = true)
        UUID hubId,

        @Schema(description = "업체 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID companyId
) {
}