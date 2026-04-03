package com.delivery.hubpath.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 경로 생성 커맨드")
public record CreateHubPathCommand (
        String departHubName,
        String arriveHubName
)
{
    public static CreateHubPathCommand of(String departHubName, String arriveHubName) {
        return new CreateHubPathCommand(departHubName, arriveHubName);
    }
}
