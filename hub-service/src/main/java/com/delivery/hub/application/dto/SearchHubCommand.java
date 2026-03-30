package com.delivery.hub.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 검색 커맨드")
public record SearchHubCommand(
        String hub_name,
        String address
){
    public static SearchHubCommand of(String hub_name, String address) {
        return new SearchHubCommand(hub_name,address);
    }
}
