package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.CreateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.SearchDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.UpdateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryManagerResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.UserId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryManagerDomainService;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryManagerDomainService deliveryManagerDomainService;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;


    @Transactional
    public DeliveryManagerResult createDeliveryManager(CreateDeliveryManagerCommand command) {
        deliveryManagerDomainService.validateWritePermission(command.requesterRole());
        UserId userId = UserId.of(command.userId());
        HubId hubId = HubId.of(command.hubId());
        deliveryManagerDomainService.validateNotDuplicate(userId);
        deliveryManagerDomainService.validateCapacityLimit(command.type(), hubId);

        int seq = deliveryManagerDomainService.calculateNextSeq(command.type(), hubId);

        DeliveryManager deliveryManager = DeliveryManager.create(
                userId,
                hubId,
                command.type(),
                seq
        );

        return DeliveryManagerResult.from(deliveryManagerRepository.save(deliveryManager));
    }

    @Transactional(readOnly = true)
    public DeliveryManagerResult getDeliveryManager(UUID deliveryManagerId, UUID requesterId, String requesterRole) {
        deliveryManagerDomainService.validateReadPermission(requesterRole);
        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(deliveryManagerId);
        deliveryManagerDomainService.validateReadIdentification(deliveryManager, requesterId, requesterRole);
        return DeliveryManagerResult.from(deliveryManager);
    }

    @Transactional
    public DeliveryManagerResult updateDeliveryManagers(UpdateDeliveryManagerCommand command) {
        deliveryManagerDomainService.validateWritePermission(command.requesterRole());
        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(command.deliveryManagerId());
        HubId hubId = HubId.of(command.hubId());
        DeliveryManagerType type = command.type();
        int seq;
        if (deliveryManager.isHubChanged(command.hubId()) || deliveryManager.isTypeChanged(type)) {
            deliveryManagerDomainService.validateCapacityLimit(type, hubId);
            seq = deliveryManagerDomainService.calculateNextSeq(type, hubId);
        } else {
            seq = deliveryManager.getSeq();
        }

        deliveryManager.update(
                hubId,
                type,
                seq
        );
        return DeliveryManagerResult.from(deliveryManagerRepository.save(deliveryManager));
    }

    @Transactional
    public void deleteDeliveryManager(UUID deliveryManagerId, UUID requesterId, String requesterRole) {
        deliveryManagerDomainService.validateWritePermission(requesterRole);

        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(deliveryManagerId);
        //배송 중, 배정 완료인 매니저는 삭제 불가
        if (deliveryAssignmentRepository.hasActiveDelivery(DeliveryManagerId.of(deliveryManagerId))) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_ACTIVE);
        }

        deliveryManager.softDelete(String.valueOf(requesterId));
    }

    @Transactional(readOnly = true)
    public Page<DeliveryManagerResult> searchDeliveryManagers(SearchDeliveryManagerCommand command) {
        deliveryManagerDomainService.validateReadPermission(command.requesterRole());

        PageRequest pageable = PageRequest.of(command.validatedPage(), command.validatedSize());

        return deliveryManagerRepository.search(command.type(), command.hubId(), command.requesterId(), command.requesterRole(), pageable)
                .map(DeliveryManagerResult::from);
    }


    private DeliveryManager findDeliveryManagerOrThrow(UUID deliveryManagerId) {
        return deliveryManagerRepository.findById(DeliveryManagerId.of(deliveryManagerId))
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_MANAGER_NOT_FOUND));
    }
}