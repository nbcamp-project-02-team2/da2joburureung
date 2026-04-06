package com.da2jobu.notificationservice.infrastructure.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String username,
        String name,
        String slackId,
        String role,
        String status
) {
}
