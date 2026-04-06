package com.da2jobu.deliveryservice.application.deliveryManager.service;


import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryAssignmentRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.repository.DeliveryManagerRepository;
import com.da2jobu.deliveryservice.domain.deliveryManager.service.DeliveryAssignmentDomainService;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class HubDeliveryAssignmentService extends DeliveryAssignmentService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final DeliveryAssignmentDomainService deliveryAssignmentDomainService;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    public HubDeliveryAssignmentService(DeliveryAssignmentRepository deliveryAssignmentRepository,
                                        DeliveryManagerRepository deliveryManagerRepository,
                                        DeliveryAssignmentDomainService deliveryAssignmentDomainService,
                                        DeliveryRepository deliveryRepository,
                                        DeliveryRouteRecordRepository deliveryRouteRecordRepository) {
        super(deliveryAssignmentRepository);
        this.deliveryManagerRepository = deliveryManagerRepository;
        this.deliveryAssignmentDomainService = deliveryAssignmentDomainService;
        this.deliveryRepository = deliveryRepository;
        this.deliveryRouteRecordRepository = deliveryRouteRecordRepository;
    }

    /**
     * 허브 배송 담당자
     * 대기시간, 순번 기준 라운드로빈 방식
     * 배송 시작 지점까지 이동하는 것에 대한 시공간적 제약은 무시 (발제문 참고)
     */
    @Transactional
    public void assignHubDelivery(DeliveryId deliveryId, DeliveryRouteRecordId deliveryRouteRecordId, UUID startHubId) {
        List<DeliveryManager> availableHubDeliveryManagers = deliveryManagerRepository.findHubDeliveryManagersWithNoAssignment();
        if (availableHubDeliveryManagers.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_MANAGER_NOT_FOUND);
        }

        DeliveryManager manager = deliveryAssignmentDomainService.selectBestCandidate(availableHubDeliveryManagers);

        DeliveryAssignment assignment = DeliveryAssignment.create(
                manager.getDeliveryManagerId(),
                deliveryId,
                deliveryRouteRecordId,
                HubId.of(startHubId)
        );
        deliveryAssignmentRepository.save(assignment);

        // 배정된 담당자로 delivery, deliveryRouteRecord 업데이트
        UUID managerId = manager.getDeliveryManagerId().getDeliveryManagerId();
        Delivery delivery = deliveryRepository.findByDeliveryIdAndDeletedAtIsNull(deliveryId.getDeliveryId())
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
        delivery.updateManagerId(managerId);

        DeliveryRouteRecord routeRecord = deliveryRouteRecordRepository
                .findByDeliveryRouteRecordIdAndDeletedAtIsNull(deliveryRouteRecordId.getDeliveryRouteRecordId())
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_ROUTE_RECORD_NOT_FOUND));
        routeRecord.updateManagerId(managerId);
    }

}
