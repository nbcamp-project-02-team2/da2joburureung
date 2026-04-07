package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "허브 상세 응답")
public record HubResponse(
        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111")
        @JsonProperty("hub_id")
        UUID hubId,

        @JsonProperty("hub_name") String hubName,

        @JsonProperty("address") String address,

        @Schema(description = "위도", example = "37.566500")
        @JsonProperty("latitude")
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.978000")
        @JsonProperty("longitude")
        BigDecimal longitude
) {
}