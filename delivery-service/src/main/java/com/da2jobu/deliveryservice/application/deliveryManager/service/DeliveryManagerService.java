package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.CreateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.SearchDeliveryAssignmentCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.SearchDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.UpdateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryAssignmentResult;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryManagerResult;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.UserId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryManagerDomainService;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.HubServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoByIdDto;
import common.exception.CustomException;
import common.exception.ErrorCode;
import feign.FeignException;
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
    private final UserServiceClient userServiceClient;
    private final HubServiceClient hubServiceClient;


    @Transactional
    public DeliveryManagerResult createDeliveryManager(CreateDeliveryManagerCommand command) {
        validateUserIsDeliveryManager(command.userId());
        validateHubExists(command.hubId());
        validateHubAccess(command.requesterId(), command.requesterRole(), command.type(), HubId.of(command.hubId()));

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
        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(deliveryManagerId);
        deliveryManagerDomainService.validateReadIdentification(deliveryManager, requesterId, requesterRole);
        return DeliveryManagerResult.from(deliveryManager);
    }

    @Transactional
    public DeliveryManagerResult updateDeliveryManagers(UpdateDeliveryManagerCommand command) {
        validateHubExists(command.hubId());
        validateHubAccess(command.requesterId(), command.requesterRole(), command.type(), HubId.of(command.hubId()));
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
        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(deliveryManagerId);
        validateHubAccess(requesterId, requesterRole, deliveryManager.getType(), deliveryManager.getHubId());
        //배송 중, 배정 완료인 매니저는 삭제 불가
        if (deliveryAssignmentRepository.hasActiveDelivery(DeliveryManagerId.of(deliveryManagerId))) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_ACTIVE);
        }

        deliveryManager.softDelete(String.valueOf(requesterId));
    }

    @Transactional(readOnly = true)
    public Page<DeliveryManagerResult> searchDeliveryManagers(SearchDeliveryManagerCommand command) {
        PageRequest pageable = PageRequest.of(command.validatedPage(), command.validatedSize());

        return deliveryManagerRepository.search(command.type(), command.hubId(), command.requesterId(), command.requesterRole(), pageable)
                .map(DeliveryManagerResult::from);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryAssignmentResult> searchDeliveryAssignments(SearchDeliveryAssignmentCommand command) {
        DeliveryManager deliveryManager = findDeliveryManagerOrThrow(command.deliveryManagerId());
        deliveryManagerDomainService.validateReadIdentification(deliveryManager, command.requesterId(), command.requesterRole());

        PageRequest pageable = PageRequest.of(command.validatedPage(), command.validatedSize());

        return deliveryAssignmentRepository
                .findByManagerId(deliveryManager.getDeliveryManagerId(), command.status(), pageable)
                .map(DeliveryAssignmentResult::from);
    }

    private DeliveryManager findDeliveryManagerOrThrow(UUID deliveryManagerId) {
        return deliveryManagerRepository.findById(DeliveryManagerId.of(deliveryManagerId))
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_MANAGER_NOT_FOUND));
    }

    private void validateUserIsDeliveryManager(UUID userId) {
        try {
            UserInfoByIdDto user = userServiceClient.getUserByUserId(userId);
            if (user == null) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }
            if (!"DELIVERY_MANAGER".equals(user.userRole())) {
                throw new CustomException(ErrorCode.DELIVERY_MANAGER_INVALID_ROLE);
            }
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.USER_SERVICE_ERROR);
        }
    }

    private void validateHubExists(UUID hubId) {
        try {
            hubServiceClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.HUB_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.HUB_SERVICE_ERROR);
        }
    }

    private void validateHubAccess(UUID requesterId, String requesterRole, DeliveryManagerType type, HubId targetHubId) {
        if ("MASTER".equals(requesterRole) || DeliveryManager.isHubDeliveryManager(type, targetHubId)) {
            return;
        }
        UserInfoByIdDto requester = userServiceClient.getUserByUserId(requesterId);
        if (requester == null || requester.hubId() == null) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (!requester.hubId().equals(targetHubId.getHubId())) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_HUB_MISMATCH);
        }
    }
}