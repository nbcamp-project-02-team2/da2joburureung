package com.da2jobu.aiservice.interfaces.controller.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AiResponse(
    List<AiResultDto> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public record AiResultDto(
        UUID resultId,
        UUID deliveryId,
        LocalDateTime estimatedArrivalTime,
        String routeSummary,
        String weatherSafetyComment,
        LocalDateTime createdAt
    ) {}
}
