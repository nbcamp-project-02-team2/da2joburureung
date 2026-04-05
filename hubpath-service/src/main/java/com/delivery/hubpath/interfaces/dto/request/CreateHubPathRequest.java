package com.delivery.hubpath.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "허브 경로 요청")
public record CreateHubPathRequest(

    @NotBlank(message = "출발 허브 이름은 필수 입니다")
    @Schema(description = "허브 이름", example = "경기 남부 허브")
    String departHubName,

    @NotBlank(message = "도착 허브 이름은 필수 입니다.")
    @Schema(description = "허브 이름", example = "경기 남부 허브")
    String arriveHubName

){}
