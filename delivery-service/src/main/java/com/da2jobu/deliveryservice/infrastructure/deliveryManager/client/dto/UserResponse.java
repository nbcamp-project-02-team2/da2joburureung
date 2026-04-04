package com.da2jobu.deliveryservice.infrastructure.deliveryManager.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String role
) {
}