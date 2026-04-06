package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record HubPathResponseDto(
        UUID hubPathId,
        UUID departHubId,
        String departHubName,
        UUID arriveHubId,
        String arriveHubName,
        UUID middleHubId,
        String middleHubName,
        BigDecimal firstDistance,
        Integer firstDuration,
        BigDecimal secondDistance,
        Integer secondDuration,
        BigDecimal distance,
        Integer duration,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        String deletedBy,
        LocalDateTime deletedAt
) {
}
