package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;

import java.util.UUID;

public record HubDeliveryAssignmentResult(
        UUID deliveryAssignmentId,
        UUID deliveryManagerId,
        int managerSeq
) {
    public static HubDeliveryAssignmentResult from(DeliveryAssignment assignment, DeliveryManager manager) {
        return new HubDeliveryAssignmentResult(
                assignment.getDeliveryAssignmentId().getDeliveryAssignmentId(),
                manager.getDeliveryManagerId().getDeliveryManagerId(),
                manager.getSeq()
        );
    }
}
