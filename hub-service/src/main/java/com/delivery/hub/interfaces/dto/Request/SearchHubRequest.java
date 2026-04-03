package com.delivery.hub.interfaces.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 검색 조건")
public record SearchHubRequest (
    @Schema(description = "허브 이름 검색어")
    String hub_name,

    @Schema(description = "주소 검색어")
    String address
){}