package com.delivery.hubpath.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "허브 경로 요청")
public record CreateHubPathRequest(

        @NotNull(message = "출발 허브 ID는 필수입니다")
        @Schema(description = "허브 ID")
        UUID departHubId,

        @NotNull(message = "도착 허브 ID는 필수입니다")
        @Schema(description = "허브 ID")
        UUID arriveHubId

){}