package com.da2jobu.notificationservice.infrastructure.client.dto;

import java.util.List;

public record UserPageResponse(
        List<UserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
