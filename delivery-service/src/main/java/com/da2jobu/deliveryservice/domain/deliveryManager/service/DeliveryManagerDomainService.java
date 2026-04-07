package com.da2jobu.deliveryservice.domain.deliveryManager.service;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.UserId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeliveryManagerDomainService {

    private static final int MAX_CAPACITY = 10;

    private final DeliveryManagerRepository deliveryManagerRepository;

    //중복 검증
    public void validateNotDuplicate(UserId userId) {
        if (deliveryManagerRepository.existsByUserId(userId)) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_ALREADY_EXISTS);
        }
    }

    //현재 수용 용량 검증
    public void validateCapacityLimit(DeliveryManagerType type, HubId hubId) {
        long count = DeliveryManager.isHubDeliveryManager(type,hubId)
                ? deliveryManagerRepository.countActiveByTypeAndNullHub(type)
                : deliveryManagerRepository.countActiveByTypeAndHub(type, hubId);

        if (count >= MAX_CAPACITY) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_LIMIT_EXCEEDED);
        }
    }

    // 순번 지정
    public int calculateNextSeq(DeliveryManagerType type, HubId hubId) {
        int maxSeq = DeliveryManager.isHubDeliveryManager(type,hubId)
                ? deliveryManagerRepository.findMaxSeqByTypeAndNullHubForUpdate(type)
                : deliveryManagerRepository.findMaxSeqByTypeAndHubForUpdate(type, hubId);

        return maxSeq + 1;
    }
    // 배송담당자 본인 확인
    public void validateReadIdentification(DeliveryManager deliveryManager,UUID requesterId,String requesterRole) {
        if ("DELIVERY_MANAGER".equals(requesterRole) && !deliveryManager.getUserId().getUserId().equals(requesterId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}