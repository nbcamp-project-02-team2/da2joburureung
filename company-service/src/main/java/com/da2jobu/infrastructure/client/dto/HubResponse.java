package com.da2jobu.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "허브 서비스 응답 DTO")
public record HubResponse(
        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID hubId,

        @Schema(description = "허브명", example = "서울 허브")
        String name,

        @Schema(description = "허브 주소", example = "서울특별시 송파구 송파대로 55")
        String address
) {
}