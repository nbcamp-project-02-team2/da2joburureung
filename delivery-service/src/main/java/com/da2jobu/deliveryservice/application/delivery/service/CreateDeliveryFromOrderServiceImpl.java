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
import com.da2jobu.deliveryservice.infrastructure.delivery.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.HubPathServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.HubServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.OrderServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.ProductServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.CompanyInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubPathResponseDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubResponse;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.LocationInfo;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.OrderInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.ProductInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoByIdDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoDto;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

        // createdBy(username)으로 사용자 조회
        UserInfoDto receiverUser = userServiceClient.getUserByUsername(command.createdBy());

        // supplierId, receiverId로 업체 조회
        CompanyInfoDto supplierCompany = companyServiceClient.getCompany(command.supplierId());
        CompanyInfoDto receiverCompany = companyServiceClient.getCompany(command.receiverId());

        // 공급업체 허브 / 수령업체 허브 조회
        HubResponse originHub = getHub(supplierCompany.hubId());
        HubResponse destinationHub = getHub(receiverCompany.hubId());

        // 허브 경로 조회
        HubPathResponseDto hubPath = getHubPath(originHub.name(), destinationHub.name());
        List<HubPathResponseDto.StepDto> sortedSteps = getSortedSteps(hubPath);

        // 배송 담당자
        UUID companyDeliveryManagerId = null;
        UUID hubDeliveryManagerId = null;

        int totalExpectedDuration = defaultDuration(hubPath.totalDuration());

        // Delivery 생성
        CreateDeliveryCommand createDeliveryCommand = new CreateDeliveryCommand(
                command.orderId(),
                originHub.hubId(),
                destinationHub.hubId(),
                receiverCompany.address(),
                receiverUser.name(),
                receiverUser.slackId(),
                supplierCompany.companyId(),
                receiverCompany.companyId(),
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
                sortedSteps,
                totalExpectedDuration,
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
                destinationHub,
                sortedSteps,
                companyDeliveryManagerId
        );

        DeliveryRouteRecord firstHubRoute = findFirstHubRoute(routeRecords);
        // 허브 배송 담당자 배정
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
            List<HubPathResponseDto.StepDto> sortedSteps,
            Integer totalExpectedDuration,
            UUID hubDeliveryManagerId,
            UUID companyDeliveryManagerId
    ) {
        List<DeliveryRouteRecord> routes = new ArrayList<>();
        int sequence = 0;

        LocationInfo supplierCompanyLocation = toCompanyLocation(supplierCompany);
        LocationInfo originHubLocation = toHubLocation(originHub);
        LocationInfo destinationHubLocation = toHubLocation(destinationHub);
        LocationInfo receiverCompanyLocation = toCompanyLocation(receiverCompany);

        Map<UUID, HubResponse> hubCache = new HashMap<>();
        hubCache.put(originHub.hubId(), originHub);
        hubCache.put(destinationHub.hubId(), destinationHub);

        // 0. 공급업체 -> 출발 허브
        routes.add(createRouteRecord(
                deliveryId,
                sequence++,
                supplierCompanyLocation,
                originHubLocation,
                BigDecimal.ZERO,
                0,
                DeliveryRouteStatus.HUB_WAITING,
                null,
                totalExpectedDuration
        ));

        // 1. 허브 -> 허브 경로 (steps 기반)
        for (HubPathResponseDto.StepDto step : sortedSteps) {
            HubResponse startHub = hubCache.computeIfAbsent(step.startHubId(), this::getHub);
            HubResponse endHub = hubCache.computeIfAbsent(step.endHubId(), this::getHub);

            LocationInfo startHubLocation = toHubLocation(startHub);
            LocationInfo endHubLocation = toHubLocation(endHub);

            UUID assignedHubManagerId = (step.stepOrder() == 1) ? hubDeliveryManagerId : null;

            routes.add(createRouteRecord(
                    deliveryId,
                    sequence++,
                    startHubLocation,
                    endHubLocation,
                    defaultDistance(step.distance()),
                    defaultDuration(step.duration()),
                    DeliveryRouteStatus.HUB_WAITING,
                    assignedHubManagerId,
                    calculateRemainDuration(step.stepOrder(), sortedSteps)
            ));
        }

        // 마지막. 도착 허브 -> 수령 업체
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
            HubResponse destinationHub,
            List<HubPathResponseDto.StepDto> sortedSteps,
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

        // AI 요약용 origin은 "최종 허브" 기준
        DeliveryLocationPayload originPayload = new DeliveryLocationPayload(
                destinationHub.name(),
                destinationHub.address(),
                destinationHub.latitude(),
                destinationHub.longitude()
        );

        List<DeliveryLocationPayload> waypointPayloads = buildWaypointPayloads(sortedSteps);

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

    private List<DeliveryLocationPayload> buildWaypointPayloads(List<HubPathResponseDto.StepDto> sortedSteps) {
        List<DeliveryLocationPayload> waypoints = new ArrayList<>();
        Map<UUID, HubResponse> hubCache = new HashMap<>();
        LinkedHashSet<UUID> waypointHubIds = new LinkedHashSet<>();

        for (HubPathResponseDto.StepDto step : sortedSteps) {
            waypointHubIds.add(step.endHubId());
        }

        for (UUID hubId : waypointHubIds) {
            HubResponse hub = hubCache.computeIfAbsent(hubId, this::getHub);
            waypoints.add(new DeliveryLocationPayload(
                    hub.name(),
                    hub.address(),
                    hub.latitude(),
                    hub.longitude()
            ));
        }

        return waypoints;
    }

    private List<HubPathResponseDto.StepDto> getSortedSteps(HubPathResponseDto hubPath) {
        if (hubPath == null || hubPath.steps() == null || hubPath.steps().isEmpty()) {
            throw new IllegalStateException("허브 경로(step) 정보가 없습니다.");
        }

        return hubPath.steps().stream()
                .sorted(Comparator.comparingInt(HubPathResponseDto.StepDto::stepOrder))
                .toList();
    }

    private Integer calculateRemainDuration(int currentStepOrder, List<HubPathResponseDto.StepDto> sortedSteps) {
        return sortedSteps.stream()
                .filter(step -> step.stepOrder() >= currentStepOrder)
                .map(HubPathResponseDto.StepDto::duration)
                .filter(duration -> duration != null)
                .reduce(0, Integer::sum);
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
                hubPathServiceClient.getHubPath(departHubName, arriveHubName);
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