package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HubPathResponseDto(
        UUID hub_path_id,
        UUID departHubId,
        String departHubName,
        UUID arriveHubId,
        String arriveHubName,
        List<StepDto> steps,
        BigDecimal totalDistance,
        Integer totalDuration,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt
) {
    public record StepDto(
            int stepOrder,
            UUID startHubId,
            String startHubName,
            UUID endHubId,
            String endHubName,
            BigDecimal distance,
            Integer duration
    ) {}
}