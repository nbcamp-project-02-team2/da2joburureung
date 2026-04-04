package com.delivery.hubpath.application.dto;

import java.util.UUID;

public record UpdateHubPathCommand(
        UUID hub_path_id,
        String departHubName,
        String arriveHubName

)
{
    public static UpdateHubPathCommand of (UUID hub_path_id, String departHubName, String arriveHubName) {
        return new UpdateHubPathCommand(hub_path_id, departHubName, arriveHubName);
    }
}