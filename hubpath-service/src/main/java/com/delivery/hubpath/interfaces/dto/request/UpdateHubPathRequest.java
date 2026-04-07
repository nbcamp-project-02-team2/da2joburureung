package com.delivery.hubpath.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "허브 경로 수정 요청")
public record UpdateHubPathRequest(

        @NotNull(message = "출발 허브 ID는 필수입니다")
        @Schema(description = "출발 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID departHubId,

        @NotNull(message = "도착 허브 ID는 필수입니다")
        @Schema(description = "도착 허브 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID arriveHubId
)
{}