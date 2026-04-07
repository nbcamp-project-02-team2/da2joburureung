package com.da2jobu.notificationservice.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AiDeliveryInfoEvent(
        UUID deliveryId,
        List<String> slackIds,
        LocalDateTime estimatedArrivalTime,
        String routeSummary,
        String weatherSafetyComment
) {}
