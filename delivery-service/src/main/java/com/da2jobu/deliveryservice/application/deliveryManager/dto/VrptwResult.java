package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import java.util.List;

public record VrptwResult(
        List<VehicleRoute> routes,
        double totalDistanceKm,
        boolean feasible    //성공여부
) {
    public static VrptwResult of(List<VehicleRoute> routes, double totalDistanceKm, boolean feasible) {
        return new VrptwResult(routes, totalDistanceKm, feasible);
    }
}
