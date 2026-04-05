package com.da2jobu.deliveryservice.infrastructure.persistence;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaDeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, DeliveryAssignmentId> {
    boolean existsByDeliveryManagerIdAndStatusIn(DeliveryManagerId deliveryManagerId, List<DeliveryAssignmentStatus> statusList);
    Optional<DeliveryAssignment> findByDeliveryAssignmentIdAndDeletedAtIsNull(DeliveryAssignmentId deliveryAssignmentId);
}
