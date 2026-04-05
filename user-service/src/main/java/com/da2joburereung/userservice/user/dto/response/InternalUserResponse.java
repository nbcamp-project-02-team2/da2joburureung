package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.User;

import java.util.UUID;

public record InternalUserResponse(
        UUID userId,
        String username,
        String name,
        String email,
        String slackId
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSlackId()
        );
    }
}
