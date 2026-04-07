package com.da2jobu.deliveryservice.domain.deliveryManager.repository;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.ManagerIdleDuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository {
    boolean hasActiveDelivery(DeliveryManagerId deliveryManagerId);
    List<ManagerIdleDuration> findIdleDurationsByManagerIds(List<DeliveryManagerId> managerIds);
    DeliveryAssignment save(DeliveryAssignment assignment);
    Optional<DeliveryAssignment> findById(DeliveryAssignmentId deliveryAssignmentId);
    Page<DeliveryAssignment> findByManagerId(DeliveryManagerId deliveryManagerId, DeliveryAssignmentStatus status, Pageable pageable);
}
