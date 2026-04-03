package com.da2jobu.deliverymanagerservice.application.service;

import com.da2jobu.deliverymanagerservice.application.dto.command.CreateDeliveryManagerCommand;
import com.da2jobu.deliverymanagerservice.application.dto.result.DeliveryManagerResult;
import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
import com.da2jobu.deliverymanagerservice.domain.service.DeliveryManagerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryManagerDomainService deliveryManagerDomainService;

    @Transactional
    public DeliveryManagerResult createDeliveryManager(CreateDeliveryManagerCommand command) {
        /**
         * todo: 유저 허브 검증
         */
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
}