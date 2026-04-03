package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String username,
        String name,
        String slackId,
        UserRole role,
        UserStatus status,
        UUID hubId,
        UUID companyId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getSlackId(),
                user.getRole(),
                user.getStatus(),
                user.getHubId(),
                user.getCompanyId()
        );
    }
}