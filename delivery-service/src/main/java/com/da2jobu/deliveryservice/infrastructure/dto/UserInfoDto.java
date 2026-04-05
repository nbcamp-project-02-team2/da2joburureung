package com.da2jobu.deliveryservice.infrastructure.dto;

import java.util.UUID;

public record UserInfoDto(
        UUID userId,
        String username,
        String name,
        String slackId,
        String role
) {
}
