package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경로 위치 타입")
public enum RouteLocationType {

    @Schema(description = "허브")
    HUB,

    @Schema(description = "업체")
    COMPANY
}
