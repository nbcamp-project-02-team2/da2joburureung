package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HubResponse(
        @JsonProperty("hubId") UUID hubId,
        @JsonProperty("name") String name,
        @JsonProperty("address") String address,
        @JsonProperty("latitude") BigDecimal latitude,
        @JsonProperty("longitude") BigDecimal longitude
) {
}