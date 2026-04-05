package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VrptwInput(
        List<CompanyDeliveryPoint> deliveryPoints,
        int vehicleCount,                           // 투입 가능한 배송 담당자 수
        long maxRouteTimeMinutes,                   // 최대 배송시간
        LocalDateTime batchStartTime
) {
    public static VrptwInput of(List<CompanyDeliveryPoint> points, int vehicleCount,
                                LocalDateTime batchStartTime) {
        return new VrptwInput(points, vehicleCount, 480, batchStartTime);
    }
}