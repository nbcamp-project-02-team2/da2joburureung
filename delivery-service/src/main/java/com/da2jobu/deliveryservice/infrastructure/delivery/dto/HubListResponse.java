package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "허브 목록 항목 응답")
public record HubListResponse(
        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID hubId
) {
}