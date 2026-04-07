package com.delivery.hubpath.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "허브 경로 검색 요청")
public record SearchHubPathRequest(
        @Schema(description = "출발 허브 이름", example = "서울 허브", nullable = true)
        String depart_hub_name,

        @Schema(description = "도착 허브 이름", example = "부산 허브", nullable = true)
        String arrive_hub_name
) {}