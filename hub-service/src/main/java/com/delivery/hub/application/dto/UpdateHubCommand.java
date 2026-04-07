package com.delivery.hub.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "허브 수정 커맨드")
public record UpdateHubCommand(
        @Schema(description = "허브 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID hubId,

        @Schema(description = "허브 이름", example = "경기 남부 허브", nullable = true)
        String hub_name,

        @Schema(description = "허브 주소", example = "경기도 이천시 부발읍 경충대로 2091", nullable = true)
        String address
) {
    public static UpdateHubCommand of(UUID hubId, String hub_name, String address) {
        return new UpdateHubCommand(hubId, hub_name, address);
    }
}