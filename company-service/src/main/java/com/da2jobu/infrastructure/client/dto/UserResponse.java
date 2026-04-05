package com.da2jobu.infrastructure.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID userId,
        String userRole,
        UUID hubId,
        UUID companyId
) {
}