package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "차량별 최적 배송 경로")
public record VehicleRoute(
        @Schema(description = "차량 인덱스", example = "0")
        int vehicleIndex,

        @Schema(description = "정렬된 배송 지점 목록")
        List<CompanyDeliveryPoint> orderedDeliveries,

        @Schema(description = "총 이동 거리(km)", example = "24.8")
        double distanceKm                          // 총 이동거리
) {
    public static VehicleRoute of(int vehicleIndex, List<CompanyDeliveryPoint> orderedDeliveries, double distanceKm) {
        return new VehicleRoute(vehicleIndex, orderedDeliveries, distanceKm);
    }
}

