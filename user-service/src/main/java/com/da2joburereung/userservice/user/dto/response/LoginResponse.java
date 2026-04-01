package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        UUID userId,
        String username,
        UserRole role,
        UserStatus status
) {
}