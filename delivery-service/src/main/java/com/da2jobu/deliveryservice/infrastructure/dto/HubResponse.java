package com.da2jobu.deliveryservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubResponse(
        UUID hubId,
        BigDecimal latitude,
        BigDecimal longitude
) {
}