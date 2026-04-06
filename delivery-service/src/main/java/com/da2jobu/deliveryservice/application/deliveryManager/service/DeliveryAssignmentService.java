package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class DeliveryAssignmentService {

    protected final DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Transactional
    public void completeDeliveryAssignment(UUID deliveryAssignmentId) {
        DeliveryAssignment assignment = deliveryAssignmentRepository
                .findById(DeliveryAssignmentId.of(deliveryAssignmentId))
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ASSIGNMENT_NOT_FOUND));

        assignment.complete();

        log.info("배송 완료 처리 - assignmentId={}, managerId={}",
                deliveryAssignmentId, assignment.getDeliveryManagerId().getDeliveryManagerId());
    }
}