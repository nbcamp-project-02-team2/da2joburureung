package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import java.util.UUID;

public record CompanyDeliveryAssignmentResult(
        UUID hubId,
        int totalDeliveries,
        int assignedManagers,
        int routeCount
) {
    public static CompanyDeliveryAssignmentResult of(UUID hubId, int totalDeliveries,
                                                     int assignedManagers, int routeCount) {
        return new CompanyDeliveryAssignmentResult(hubId, totalDeliveries, assignedManagers, routeCount);
    }
}
