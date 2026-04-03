package com.da2jobu.deliverymanagerservice.domain.service;

import com.da2jobu.deliverymanagerservice.domain.model.entity.DeliveryManager;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import com.da2jobu.deliverymanagerservice.domain.repository.DeliveryManagerRepository;
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
    // 조회 권한 검증
    public void validateReadPermission(String requesterRole) {
        if ("MASTER".equals(requesterRole) || "HUB_MANAGER".equals(requesterRole) || "DELIVERY_MANAGER".equals(requesterRole)) {
            return;
        }
        throw new CustomException(ErrorCode.FORBIDDEN);
    }

    // 배송담당자 본인 확인
    public void validateReadIdentification(DeliveryManager deliveryManager,UUID requesterId,String requesterRole) {
        if ("DELIVERY_MANAGER".equals(requesterRole) && !deliveryManager.getUserId().getUserId().equals(requesterId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // 생성/수정/삭제 권한 검증 — MASTER / HUB_MANAGER만 허용
    public void validateWritePermission(String requesterRole) {
        if ("MASTER".equals(requesterRole) || "HUB_MANAGER".equals(requesterRole)) {
            return;
        }
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
}