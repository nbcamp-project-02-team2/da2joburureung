package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserStatus;

import java.util.UUID;

public record SignupResponse(
        UUID userId,
        String username,
        UserStatus status
) {
}