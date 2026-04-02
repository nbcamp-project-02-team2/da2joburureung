package com.delivery.hub.interfaces.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateHubRequest (
    @Schema(description = "허브 이름", example = "경기 남부 허브")
    String hub_name,
    @Schema(description = "허브 주소", example = "경기도 이천시 ...")
    String address
){}
