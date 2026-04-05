package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.deliveryManager.service.HubDeliveryAssignmentService;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.CompanyInfoDto;
import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final UserServiceClient userServiceClient;
    private final CompanyServiceClient companyServiceClient;

    // DeliveryManager 모듈 완료후 수정할 예정
    private static final UUID TEMP_COMPANY_DELIVERY_MANAGER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEMP_HUB_DELIVERY_MANAGER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    // 임시 배정

    @Override
    public void execute(CreateDeliveryFromOrderCommand command) {
        log.info(
                "주문 기반 배송 생성 시작 - orderId={}, supplierId={}, receiverId={}, createdBy={}",
                command.orderId(),
                command.supplierId(),
                command.receiverId(),
                command.createdBy()
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

        // 배송 담당자 배정
        UUID companyDeliveryManagerId = TEMP_COMPANY_DELIVERY_MANAGER_ID;
        UUID hubDeliveryManagerId = TEMP_HUB_DELIVERY_MANAGER_ID;

        // 경로 고정, 추후 허브 경로 조회 로직으로 교체 예정
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
                null,
                null
        );

        CreateDeliveryResponseDto createDeliveryResponse = deliveryService.createDelivery(createDeliveryCommand);
        UUID deliveryId = createDeliveryResponse.deliveryId();

        // DeliveryRouteRecord 2건 생성
        DeliveryRouteRecord route0 = DeliveryRouteRecord.builder()
                .deliveryId(deliveryId)
                .sequence(0)
                .originId(originHubId)
                .originType(RouteLocationType.HUB)
                .destinationId(destinationHubId)
                .destinationType(RouteLocationType.HUB)
                .expectedDistanceKm(hubToHubExpectedDistance)
                .expectedDurationMin(hubToHubExpectedDuration)
                .status(DeliveryRouteStatus.WAITING)    // 수정 예정
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
                .status(DeliveryRouteStatus.WAITING)    // 수정 예정
                .deliveryManagerId(companyDeliveryManagerId)
                .realDistanceKm(null)
                .realDurationMin(null)
                .remainDurationMin(hubToCompanyExpectedDuration)
                .build();

        deliveryRouteRecordRepository.saveAll(List.of(route0, route1));

        //허브 배송 담당자 배정
        hubDeliveryAssignmentService.assignHubDelivery(
                DeliveryId.of(deliveryId),
                DeliveryRouteRecordId.of(route0.getDeliveryRouteRecordId()),
                originHubId
        );

        log.info("주문 기반 배송 생성 완료 - orderId={}, deliveryId={}", command.orderId(), deliveryId);
    }
}
