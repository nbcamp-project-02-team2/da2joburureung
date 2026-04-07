package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "VRPTW 최적화 결과")
public record VrptwResult(
        @Schema(description = "차량별 경로 목록")
        List<VehicleRoute> routes,

        @Schema(description = "총 이동 거리(km)", example = "72.4")
        double totalDistanceKm,

        @Schema(description = "배차 가능 여부", example = "true")
        boolean feasible    //성공여부
) {
    public static VrptwResult of(List<VehicleRoute> routes, double totalDistanceKm, boolean feasible) {
        return new VrptwResult(routes, totalDistanceKm, feasible);
    }
}
