package com.delivery.hub.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 생성 커맨드")
public record CreateHubCommand(
        @Schema(description = "허브 이름", example = "경기 남부 허브")
        String hub_name,

        @Schema(description = "허브 주소", example = "경기도 이천시 부발읍 중부대로 123")
        String address
) {
    public static CreateHubCommand of(String hub_name, String address) {
        return new CreateHubCommand(hub_name,address);
    }
}