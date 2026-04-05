package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String role
) {
}