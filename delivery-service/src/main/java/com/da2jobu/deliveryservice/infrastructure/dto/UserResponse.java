package com.da2jobu.deliveryservice.infrastructure.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String role
) {
}