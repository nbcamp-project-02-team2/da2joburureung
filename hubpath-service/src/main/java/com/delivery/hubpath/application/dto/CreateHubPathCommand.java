package com.delivery.hubpath.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "허브 경로 생성 커맨드")
public record CreateHubPathCommand (
        @Schema(description = "출발 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID departHubId,

        @Schema(description = "도착 허브 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID arriveHubId
)
{
    public static CreateHubPathCommand of(UUID departHubId, UUID arriveHubId) {
        return new CreateHubPathCommand(departHubId, arriveHubId);
    }
}