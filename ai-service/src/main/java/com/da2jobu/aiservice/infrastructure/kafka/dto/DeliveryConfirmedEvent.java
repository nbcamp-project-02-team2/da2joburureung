package com.da2jobu.aiservice.infrastructure.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryConfirmedEvent(
        UUID deliveryId,
        String deliveryManagerSlackId,
        String hubManagerSlackId,
        String departureHubName,
        String productName,
        String arrivalHubName,
        double departureLat,
        double departureLon,
        double arrivalLat,
        double arrivalLon,
        LocalDateTime scheduledDepartureTime
) {}
