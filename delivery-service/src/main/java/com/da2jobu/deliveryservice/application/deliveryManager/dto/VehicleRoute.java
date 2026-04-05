package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import java.util.List;

public record VehicleRoute(
        int vehicleIndex,                               // 정렬 순번
        List<CompanyDeliveryPoint> orderedDeliveries,   // 재정렬 된 업체 배송 경로
        double distanceKm                               // 총 이동거리
) {
    public static VehicleRoute of(int vehicleIndex, List<CompanyDeliveryPoint> orderedDeliveries, double distanceKm) {
        return new VehicleRoute(vehicleIndex, orderedDeliveries, distanceKm);
    }
}

