package com.delivery.hubpath.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "허브 경로 수정 커맨드")
public record UpdateHubPathCommand(
        @Schema(description = "허브 경로 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID hub_path_id,

        @Schema(description = "출발 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID departHubId,

        @Schema(description = "도착 허브 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID arriveHubId
) {
    public static UpdateHubPathCommand of(UUID hub_path_id, UUID departHubId, UUID arriveHubId) {
        return new UpdateHubPathCommand(hub_path_id, departHubId, arriveHubId);
    }
}