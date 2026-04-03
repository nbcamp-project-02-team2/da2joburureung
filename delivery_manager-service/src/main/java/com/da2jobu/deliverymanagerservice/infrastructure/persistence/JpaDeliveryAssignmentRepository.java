package com.da2jobu.deliverymanagerservice.infrastructure.persistence;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryAssignment;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaDeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, DeliveryAssignmentId> {
    boolean existsByDeliveryManagerIdAndStatusIn(DeliveryManagerId deliveryManagerId, List<DeliveryAssignmentStatus> statusList);
}
