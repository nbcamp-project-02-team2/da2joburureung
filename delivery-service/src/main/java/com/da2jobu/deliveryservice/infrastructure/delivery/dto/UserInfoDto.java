package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.util.UUID;

public record UserInfoDto(
        UUID userId,
        String username,
        String name,
        String email,
        String slackId,
        String role
) {
}
