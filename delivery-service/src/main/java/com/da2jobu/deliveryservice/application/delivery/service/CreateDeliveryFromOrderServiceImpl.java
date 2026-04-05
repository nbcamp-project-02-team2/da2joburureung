package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryCreatedEvent;
import com.da2jobu.deliveryservice.application.deliveryManager.service.HubDeliveryAssignmentService;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.CompanyInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.messaging.DeliveryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateDeliveryFromOrderServiceImpl implements CreateDeliveryFromOrderService {

    private final DeliveryService deliveryService;
    private final HubDeliveryAssignmentService hubDeliveryAssignmentService;

    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;
    private final DeliveryRepository deliveryRepository;

    private final DeliveryEventProducer deliveryEventProducer;

    private final UserServiceClient userServiceClient;
    private final CompanyServiceClient companyServiceClient;

    @Override
    public void execute(CreateDeliveryFromOrderCommand command) {
        log.info(
                "주문 기반 배송 생성 시작 - orderId={}, supplierId={}, receiverId={}, createdBy={}, desiredDeliveryAt={}",
                command.orderId(),
                command.supplierId(),
                command.receiverId(),
                command.createdBy(),
                command.desiredDeliveryAt()
        );

        if (deliveryRepository.existsByOrderIdAndDeletedAtIsNull(command.orderId())) {
            log.warn("이미 배송이 생성된 주문입니다. 중복 생성 스킵 - orderId={}", command.orderId());
            return;
        }

        // createdBy(username)로 사용자 조회
        // slackId가 null인지 확인 필요
        UserInfoDto receiverUser = userServiceClient.getUserByUsername(command.createdBy());

        // supplierId, receiverId로 업체 조회
        CompanyInfoDto supplierCompany = companyServiceClient.getCompany(command.supplierId());
        CompanyInfoDto receiverCompany = companyServiceClient.getCompany(command.receiverId());

        // 공급업체 허브 / 수령업체 허브 조회
        UUID originHubId = supplierCompany.hubId();
        UUID destinationHubId = receiverCompany.hubId();

        // 배송 담당자 배정, 초기에는 null로 설정
        // TODO: 배송 경로 배정 후에는 중간 배송 담당자 추가
        UUID companyDeliveryManagerId = null;
        UUID hubDeliveryManagerId = null;

        // TODO: 경로 고정, 추후 허브 경로 조회 로직으로 교체 예정
        BigDecimal hubToHubExpectedDistance = BigDecimal.valueOf(10.00);
        int hubToHubExpectedDuration = 60;

        BigDecimal hubToCompanyExpectedDistance = BigDecimal.valueOf(5.00);
        int hubToCompanyExpectedDuration = 30;

        int totalExpectedDuration = hubToHubExpectedDuration + hubToCompanyExpectedDuration;

        // Delivery 생성
        CreateDeliveryCommand createDeliveryCommand = new CreateDeliveryCommand(
                command.orderId(),
                originHubId,
                destinationHubId,
                receiverCompany.address(),
                receiverUser.name(),
                receiverUser.slackId(),
                companyDeliveryManagerId,
                command.requirements(),
                totalExpectedDuration,
                DeliveryStatus.HUB_WAITING,
                command.desiredDeliveryAt(),
                null,
                null
        );

        CreateDeliveryResponseDto createDeliveryResponse = deliveryService.createDelivery(createDeliveryCommand);
        UUID deliveryId = createDeliveryResponse.deliveryId();

        // DeliveryRouteRecord 2건 생성
        // TODO: hubPath와 연결해서 수정
        DeliveryRouteRecord route0 = DeliveryRouteRecord.builder()
                .deliveryId(deliveryId)
                .sequence(0)
                .originId(originHubId)
                .originType(RouteLocationType.HUB)
                .destinationId(destinationHubId)
                .destinationType(RouteLocationType.HUB)
                .expectedDistanceKm(hubToHubExpectedDistance)
                .expectedDurationMin(hubToHubExpectedDuration)
                .status(DeliveryRouteStatus.HUB_WAITING)    // 수정 예정
                .deliveryManagerId(hubDeliveryManagerId)
                .realDistanceKm(null)
                .realDurationMin(null)
                .remainDurationMin(totalExpectedDuration)
                .build();

        DeliveryRouteRecord route1 = DeliveryRouteRecord.builder()
                .deliveryId(deliveryId)
                .sequence(1)
                .originId(destinationHubId)
                .originType(RouteLocationType.HUB)
                .destinationId(receiverCompany.companyId())
                .destinationType(RouteLocationType.COMPANY)
                .expectedDistanceKm(hubToCompanyExpectedDistance)
                .expectedDurationMin(hubToCompanyExpectedDuration)
                .status(DeliveryRouteStatus.HUB_WAITING)    // 수정 예정
                .deliveryManagerId(companyDeliveryManagerId)
                .realDistanceKm(null)
                .realDurationMin(null)
                .remainDurationMin(hubToCompanyExpectedDuration)
                .build();

        deliveryRouteRecordRepository.saveAll(List.of(route0, route1));

        DeliveryCreatedEvent event = new DeliveryCreatedEvent(
                command.orderId(),
                deliveryId
        );

        //허브 배송 담당자 배정
        hubDeliveryAssignmentService.assignHubDelivery(
                DeliveryId.of(deliveryId),
                DeliveryRouteRecordId.of(route0.getDeliveryRouteRecordId()),
                originHubId
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deliveryEventProducer.publishDeliveryCreated(event);
                    log.info(
                            "주문 기반 배송 생성 완료 및 DeliveryCreatedEvent 발행 - orderId={}, deliveryId={}",
                            command.orderId(),
                            deliveryId
                    );
                }
            });
        } else {
            deliveryEventProducer.publishDeliveryCreated(event);
            log.info(
                    "주문 기반 배송 생성 완료 및 DeliveryCreatedEvent 발행 - orderId={}, deliveryId={}",
                    command.orderId(),
                    deliveryId
            );
        }


        log.info("주문 기반 배송 생성 완료 - orderId={}, deliveryId={}", command.orderId(), deliveryId);
    }
}
