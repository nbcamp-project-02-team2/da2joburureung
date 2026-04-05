package com.da2jobu.deliveryservice.domain.deliveryManager.repository;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;

public interface DeliveryAssignmentRepository {
    boolean hasActiveDelivery(DeliveryManagerId deliveryManagerId);
}
