package com.delivery.hubpath.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HubResponse {
    @JsonProperty("hub_id")
    private UUID id;
    private String hub_name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}