package com.delivery.hub.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 생성 커맨드")
public record CreateHubCommand(
        String hub_name,
        String address
) {
    public static CreateHubCommand of(String hub_name, String address) {
        return new CreateHubCommand(hub_name,address);
    }
}