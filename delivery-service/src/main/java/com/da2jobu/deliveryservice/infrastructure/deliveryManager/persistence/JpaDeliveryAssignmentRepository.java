package com.da2jobu.deliveryservice.infrastructure.deliveryManager.persistence;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaDeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, DeliveryAssignmentId> {
    boolean existsByDeliveryManagerIdAndStatusIn(DeliveryManagerId deliveryManagerId, List<DeliveryAssignmentStatus> statusList);
}
