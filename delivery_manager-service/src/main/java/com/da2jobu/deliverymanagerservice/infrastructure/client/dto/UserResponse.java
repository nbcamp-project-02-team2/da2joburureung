package com.da2jobu.deliverymanagerservice.infrastructure.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String role
) {
}