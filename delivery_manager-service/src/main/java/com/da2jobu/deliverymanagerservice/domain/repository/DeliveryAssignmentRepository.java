package com.da2jobu.deliverymanagerservice.domain.repository;

import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;

public interface DeliveryAssignmentRepository {
    boolean hasActiveDelivery(DeliveryManagerId deliveryManagerId);
}
