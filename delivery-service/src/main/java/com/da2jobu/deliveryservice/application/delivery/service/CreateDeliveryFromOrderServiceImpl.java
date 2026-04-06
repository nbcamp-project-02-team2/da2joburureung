package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryCreatedEvent;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryLocationPayload;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryPreparedEvent;
import com.da2jobu.deliveryservice.application.deliveryManager.service.HubDeliveryAssignmentService;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.*;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.*;
import com.da2jobu.deliveryservice.infrastructure.delivery.messaging.DeliveryEventProducer;
import com.da2jobu.deliveryservice.infrastructure.delivery.messaging.DeliveryPreparedEventProducer;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final DeliveryPreparedEventProducer deliveryPreparedEventProducer;

    private final UserServiceClient userServiceClient;
    private final CompanyServiceClient companyServiceClient;
    private final HubServiceClient hubServiceClient;
    private final HubPathServiceClient hubPathServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;

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
        HubResponse originHub = getHub(supplierCompany.hubId());
        HubResponse destinationHub = getHub(receiverCompany.hubId());

//        UUID originHubId = supplierCompany.hubId();
//        UUID destinationHubId = receiverCompany.hubId();

        // 허브 경로 조회
        HubPathResponseDto hubPath = getHubPath(originHub.name(), destinationHub.name());

        // 배송 담당자 배정, 초기에는 null로 설정
        // TODO: 배송 경로 배정 후에는 중간 배송 담당자 추가
        UUID companyDeliveryManagerId = null;
        UUID hubDeliveryManagerId = null;

        // 총 예상 시간 계산
        int totalExpectedDuration = defaultDuration(hubPath.duration());

        // Delivery 생성
        CreateDeliveryCommand createDeliveryCommand = new CreateDeliveryCommand(
                command.orderId(),
                originHub.hubId(),
                destinationHub.hubId(),
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

        // DeliveryRouteRecord 생성
        List<DeliveryRouteRecord> routeRecords = createRouteRecords(
                deliveryId,
                supplierCompany,
                receiverCompany,
                originHub,
                destinationHub,
                hubPath,
                hubDeliveryManagerId,
                companyDeliveryManagerId
        );

        deliveryRouteRecordRepository.saveAll(routeRecords);

        DeliveryCreatedEvent createdEvent = new DeliveryCreatedEvent(
                command.orderId(),
                deliveryId
        );

        DeliveryPreparedEvent preparedEvent = createDeliveryPreparedEvent(
                deliveryId,
                command,
                receiverUser,
                receiverCompany,
                originHub,
                destinationHub,
                hubPath,
                companyDeliveryManagerId
        );

        // 첫 번째 허브 구간 담당자 배정
        DeliveryRouteRecord firstHubRoute = findFirstHubRoute(routeRecords);
        if (firstHubRoute != null) {
            hubDeliveryAssignmentService.assignHubDelivery(
                    DeliveryId.of(deliveryId),
                    DeliveryRouteRecordId.of(firstHubRoute.getDeliveryRouteRecordId()),
                    originHub.hubId()
            );
        }

        publishEventsAfterCommit(command.orderId(), deliveryId, createdEvent, preparedEvent);

        log.info("주문 기반 배송 생성 완료 - orderId={}, deliveryId={}", command.orderId(), deliveryId);
    }

    private List<DeliveryRouteRecord> createRouteRecords(
            UUID deliveryId,
            CompanyInfoDto supplierCompany,
            CompanyInfoDto receiverCompany,
            HubResponse originHub,
            HubResponse destinationHub,
            HubPathResponseDto hubPath,
            UUID hubDeliveryManagerId,
            UUID companyDeliveryManagerId
    ) {
        List<DeliveryRouteRecord> routes = new ArrayList<>();
        int sequence = 0;

        LocationInfo supplierCompanyLocation = toCompanyLocation(supplierCompany);
        LocationInfo originHubLocation = toHubLocation(originHub);
        LocationInfo destinationHubLocation = toHubLocation(destinationHub);
        LocationInfo receiverCompanyLocation = toCompanyLocation(receiverCompany);

        // 0. 공급 업체 -> 출발 허브
        routes.add(createRouteRecord(
                deliveryId,
                sequence++,
                supplierCompanyLocation,
                originHubLocation,
                BigDecimal.ZERO,
                0,
                DeliveryRouteStatus.HUB_WAITING,
                null,
                defaultDuration(hubPath.duration())
        ));

        // 1. 출발 허브 -> 경유 허브 / 도착 허브
        if (hubPath.middleHubId() != null) {
            HubResponse middleHub = getHub(hubPath.middleHubId());
            LocationInfo middleHubLocation = toHubLocation(middleHub);

            routes.add(createRouteRecord(
                    deliveryId,
                    sequence++,
                    originHubLocation,
                    middleHubLocation,
                    defaultDistance(hubPath.firstDistance()),
                    defaultDuration(hubPath.firstDuration()),
                    DeliveryRouteStatus.HUB_WAITING,
                    hubDeliveryManagerId,
                    defaultDuration(hubPath.duration())
            ));

            // 2. 경유 허브 -> 도착 허브
            routes.add(createRouteRecord(
                    deliveryId,
                    sequence++,
                    middleHubLocation,
                    destinationHubLocation,
                    defaultDistance(hubPath.secondDistance()),
                    defaultDuration(hubPath.secondDuration()),
                    DeliveryRouteStatus.HUB_WAITING,
                    null,
                    defaultDuration(hubPath.secondDuration())
            ));
        } else {
            routes.add(createRouteRecord(
                    deliveryId,
                    sequence++,
                    originHubLocation,
                    destinationHubLocation,
                    defaultDistance(hubPath.distance()),
                    defaultDuration(hubPath.duration()),
                    DeliveryRouteStatus.HUB_WAITING,
                    hubDeliveryManagerId,
                    defaultDuration(hubPath.duration())
            ));
        }

        // 마지막, 도착 허브 -> 수령 업체
        routes.add(createRouteRecord(
                deliveryId,
                sequence,
                destinationHubLocation,
                receiverCompanyLocation,
                BigDecimal.ZERO,
                0,
                DeliveryRouteStatus.HUB_WAITING,
                companyDeliveryManagerId,
                0
        ));

        return routes;
    }

    private DeliveryRouteRecord createRouteRecord(
            UUID deliveryId,
            Integer sequence,
            LocationInfo origin,
            LocationInfo destination,
            BigDecimal expectedDistanceKm,
            Integer expectedDurationMin,
            DeliveryRouteStatus status,
            UUID deliveryManagerId,
            Integer remainDurationMin
    ) {
        return DeliveryRouteRecord.builder()
                .deliveryId(deliveryId)
                .sequence(sequence)
                .originId(origin.id())
                .originType(origin.type())
                .originLatitude(toDouble(origin.latitude()))
                .originLongitude(toDouble(origin.longitude()))
                .destinationId(destination.id())
                .destinationType(destination.type())
                .destinationLatitude(toDouble(destination.latitude()))
                .destinationLongitude(toDouble(destination.longitude()))
                .expectedDistanceKm(defaultDistance(expectedDistanceKm))
                .expectedDurationMin(defaultDuration(expectedDurationMin))
                .status(status)
                .deliveryManagerId(deliveryManagerId)
                .realDistanceKm(null)
                .realDurationMin(null)
                .remainDurationMin(defaultDuration(remainDurationMin))
                .build();
    }

    private DeliveryPreparedEvent createDeliveryPreparedEvent(
            UUID deliveryId,
            CreateDeliveryFromOrderCommand command,
            UserInfoDto ordererUser,
            CompanyInfoDto receiverCompany,
            HubResponse originHub,
            HubResponse destinationHub,
            HubPathResponseDto hubPath,
            UUID companyDeliveryManagerId
    ) {
        OrderInfoDto order = orderServiceClient.getOrder(command.orderId());
        ProductInfoDto product = productServiceClient.getProduct(order.productId());

        String productInfo = product.name() + " " + order.quantity() + "개";

        UserInfoByIdDto deliveryManager = null;
        if (companyDeliveryManagerId != null) {
            CommonResponse<UserInfoByIdDto> managerResponse = userServiceClient.getUserByUserId(companyDeliveryManagerId);
            deliveryManager = managerResponse.getData();
        }

        // AI 요약용 origin은 "최종 허브" 기준으로 맞춤
        DeliveryLocationPayload originPayload = new DeliveryLocationPayload(
                destinationHub.name(),
                destinationHub.address(),
                destinationHub.latitude(),
                destinationHub.longitude()
        );

        List<DeliveryLocationPayload> waypointPayloads = buildWaypointPayloads(hubPath, destinationHub);

        DeliveryLocationPayload destinationPayload = new DeliveryLocationPayload(
                null,
                receiverCompany.address(),
                receiverCompany.latitude(),
                receiverCompany.longitude()
        );

        return new DeliveryPreparedEvent(
                deliveryId,
                command.orderId(),
                ordererUser.name(),
                ordererUser.email(),
                order.createdAt(),
                productInfo,
                command.requirements(),
                originPayload,
                waypointPayloads,
                destinationPayload,
                deliveryManager != null ? deliveryManager.slackId() : null,
                deliveryManager != null ? deliveryManager.name() : null,
                deliveryManager != null ? deliveryManager.email() : null
        );
    }

    private List<DeliveryLocationPayload> buildWaypointPayloads(HubPathResponseDto hubPath, HubResponse destinationHub) {
        List<DeliveryLocationPayload> waypoints = new ArrayList<>();

        if (hubPath.middleHubId() != null) {
            HubResponse middleHub = getHub(hubPath.middleHubId());
            waypoints.add(new DeliveryLocationPayload(
                    middleHub.name(),
                    middleHub.address(),
                    middleHub.latitude(),
                    middleHub.longitude()
            ));
        }

        waypoints.add(new DeliveryLocationPayload(
                destinationHub.name(),
                destinationHub.address(),
                destinationHub.latitude(),
                destinationHub.longitude()
        ));

        return waypoints;
    }

    private DeliveryRouteRecord findFirstHubRoute(List<DeliveryRouteRecord> routeRecords) {
        return routeRecords.stream()
                .filter(route ->
                        route.getOriginType() == RouteLocationType.HUB &&
                                route.getDestinationType() == RouteLocationType.HUB
                )
                .findFirst()
                .orElse(null);
    }

    private void publishEventsAfterCommit(
            UUID orderId,
            UUID deliveryId,
            DeliveryCreatedEvent createdEvent,
            DeliveryPreparedEvent preparedEvent
    ) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deliveryEventProducer.publishDeliveryCreated(createdEvent);
                    deliveryPreparedEventProducer.publish(preparedEvent);
                    log.info("이벤트 발행 완료 - orderId={}, deliveryId={}", orderId, deliveryId);
                }
            });
        } else {
            deliveryEventProducer.publishDeliveryCreated(createdEvent);
            deliveryPreparedEventProducer.publish(preparedEvent);
            log.info("이벤트 발행 완료 - orderId={}, deliveryId={}", orderId, deliveryId);
        }
    }

    private HubResponse getHub(UUID hubId) {
        CommonResponse<HubResponse> response = hubServiceClient.getHub(hubId);
        return response.getData();
    }

    private HubPathResponseDto getHubPath(String departHubName, String arriveHubName) {
        CommonResponse<HubPathResponseDto> response =
                hubPathServiceClient.getHubPath("search", departHubName, arriveHubName);
        return response.getData();
    }

    private LocationInfo toCompanyLocation(CompanyInfoDto company) {
        return new LocationInfo(
                company.companyId(),
                RouteLocationType.COMPANY,
                null,
                company.address(),
                company.latitude(),
                company.longitude()
        );
    }

    private LocationInfo toHubLocation(HubResponse hub) {
        return new LocationInfo(
                hub.hubId(),
                RouteLocationType.HUB,
                hub.name(),
                hub.address(),
                hub.latitude(),
                hub.longitude()
        );
    }

    private BigDecimal defaultDistance(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer defaultDuration(Integer value) {
        return value != null ? value : 0;
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}
