package com.da2joburereung.userservice.user.dto.request;

import com.da2joburereung.userservice.user.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record SignupRequest(

        @NotBlank
        @Pattern(regexp = "^[a-z0-9]{4,10}$")
        String username,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,15}$"
        )
        String password,

        @NotBlank
        String name,

        @NotBlank
        String slackId,

        @NotNull
        UserRole role,

        UUID hubId,
        UUID companyId
) {
}