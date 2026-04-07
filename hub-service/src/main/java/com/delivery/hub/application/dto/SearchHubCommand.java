package com.delivery.hub.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "허브 검색 커맨드")
public record SearchHubCommand(

        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111", nullable = true)
        UUID hub_id,

        @Schema(description = "허브 이름 검색어", example = "경기", nullable = true)
        String hub_name,

        @Schema(description = "주소 검색어", example = "이천", nullable = true)
        String address
){
    public static SearchHubCommand of(UUID hub_id,String hub_name, String address) {
        return new SearchHubCommand(hub_id,hub_name,address);
    }
}
