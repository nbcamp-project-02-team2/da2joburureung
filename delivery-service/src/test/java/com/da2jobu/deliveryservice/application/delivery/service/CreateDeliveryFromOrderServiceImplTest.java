package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryCreatedEvent;
import com.da2jobu.deliveryservice.application.delivery.event.DeliveryPreparedEvent;
import com.da2jobu.deliveryservice.application.deliveryManager.service.HubDeliveryAssignmentService;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
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
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.OrderInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.ProductInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoDto;
import com.da2jobu.deliveryservice.infrastructure.delivery.messaging.DeliveryEventProducer;
import com.da2jobu.deliveryservice.infrastructure.delivery.messaging.DeliveryPreparedEventProducer;
import common.dto.CommonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateDeliveryFromOrderServiceImplTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private HubDeliveryAssignmentService hubDeliveryAssignmentService;

    @Mock
    private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CompanyServiceClient companyServiceClient;

    @Mock
    private HubServiceClient hubServiceClient;

    @Mock
    private HubPathServiceClient hubPathServiceClient;

    @Mock
    private OrderServiceClient orderServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private DeliveryEventProducer deliveryEventProducer;

    @Mock
    private DeliveryPreparedEventProducer deliveryPreparedEventProducer;

    @InjectMocks
    private CreateDeliveryFromOrderServiceImpl createDeliveryFromOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("주문 이벤트 기반으로 Delivery 생성 및 DeliveryRouteRecord 3건 생성")
    void execute_success() {
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID supplierHubId = UUID.randomUUID();
        UUID receiverHubId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "문 앞에 놔주세요",
                "receiver_manager",
                desiredDeliveryAt
        );

        UserInfoDto userInfoDto = new UserInfoDto(
                UUID.randomUUID(),
                "receiver_manager",
                "김담당",
                "kim@test.com",
                "U12345678",
                "COMPANY_MANAGER"
        );

        CompanyInfoDto supplierCompany = new CompanyInfoDto(
                supplierId,
                supplierHubId,
                "서울특별시 공급업체 주소",
                BigDecimal.valueOf(37.123456),
                BigDecimal.valueOf(127.123456)
        );

        CompanyInfoDto receiverCompany = new CompanyInfoDto(
                receiverId,
                receiverHubId,
                "서울특별시 수령업체 주소",
                BigDecimal.valueOf(37.654321),
                BigDecimal.valueOf(127.654321)
        );

        HubResponse originHub = new HubResponse(
                supplierHubId,
                "경기 북부 센터",
                "서울특별시 허브 주소",
                BigDecimal.valueOf(37.500000),
                BigDecimal.valueOf(127.500000)
        );

        HubResponse destinationHub = new HubResponse(
                receiverHubId,
                "부산광역시 센터",
                "부산광역시 허브 주소",
                BigDecimal.valueOf(35.100000),
                BigDecimal.valueOf(129.100000)
        );

        HubPathResponseDto hubPath = new HubPathResponseDto(
                UUID.randomUUID(),
                supplierHubId,
                "경기 북부 센터",
                receiverHubId,
                "부산광역시 센터",
                null,
                null,
                null,
                null,
                null,
                null,
                BigDecimal.valueOf(10.00),
                60,
                null,
                null,
                null,
                null,
                null,
                null
        );

        OrderInfoDto orderInfoDto = new OrderInfoDto(
                orderId,
                UUID.randomUUID(),
                50,
                BigDecimal.valueOf(12000),
                BigDecimal.valueOf(600000),
                "CREATED",
                supplierId,
                receiverId,
                deliveryId,
                LocalDateTime.of(2026, 4, 5, 10, 0)
        );

        ProductInfoDto productInfoDto = new ProductInfoDto(
                orderInfoDto.productId(),
                supplierId,
                supplierHubId,
                "마른 오징어",
                BigDecimal.valueOf(12000),
                100,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        CreateDeliveryResponseDto createDeliveryResponseDto = new CreateDeliveryResponseDto(
                deliveryId,
                orderId,
                DeliveryStatus.HUB_WAITING
        );

        CommonResponse<HubResponse> originHubResponse = success(originHub);
        CommonResponse<HubResponse> destinationHubResponse = success(destinationHub);
        CommonResponse<HubPathResponseDto> hubPathResponse = success(hubPath);

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);
        when(userServiceClient.getUserByUsername("receiver_manager")).thenReturn(userInfoDto);
        when(companyServiceClient.getCompany(supplierId)).thenReturn(supplierCompany);
        when(companyServiceClient.getCompany(receiverId)).thenReturn(receiverCompany);
        when(hubServiceClient.getHub(supplierHubId)).thenReturn(originHubResponse);
        when(hubServiceClient.getHub(receiverHubId)).thenReturn(destinationHubResponse);
        when(hubPathServiceClient.getHubPath("search", "경기 북부 센터", "부산광역시 센터"))
                .thenReturn(hubPathResponse);
        when(orderServiceClient.getOrder(orderId)).thenReturn(orderInfoDto);
        when(productServiceClient.getProduct(orderInfoDto.productId())).thenReturn(productInfoDto);
        when(deliveryService.createDelivery(any())).thenReturn(createDeliveryResponseDto);
        when(deliveryRouteRecordRepository.saveAll(any())).thenAnswer(invocation -> {
            List<DeliveryRouteRecord> records = invocation.getArgument(0);
            assignRouteRecordIds(records);
            return records;
        });

        createDeliveryFromOrderService.execute(command);

        verify(userServiceClient, times(1)).getUserByUsername("receiver_manager");
        verify(companyServiceClient, times(1)).getCompany(supplierId);
        verify(companyServiceClient, times(1)).getCompany(receiverId);
        verify(hubServiceClient, times(1)).getHub(supplierHubId);
        verify(hubServiceClient, times(1)).getHub(receiverHubId);
        verify(hubPathServiceClient, times(1)).getHubPath("search", "경기 북부 센터", "부산광역시 센터");
        verify(orderServiceClient, times(1)).getOrder(orderId);
        verify(productServiceClient, times(1)).getProduct(orderInfoDto.productId());
        verify(deliveryService, times(1)).createDelivery(any());
        verify(deliveryEventProducer, times(1)).publishDeliveryCreated(any());
        verify(deliveryPreparedEventProducer, times(1)).publish(any());
        verify(hubDeliveryAssignmentService, times(1))
                .assignHubDelivery(any(), any(), eq(supplierHubId));

        ArgumentCaptor<List<DeliveryRouteRecord>> routeRecordCaptor = ArgumentCaptor.forClass(List.class);
        verify(deliveryRouteRecordRepository, times(1)).saveAll(routeRecordCaptor.capture());

        List<DeliveryRouteRecord> savedRecords = routeRecordCaptor.getValue();

        assertThat(savedRecords).hasSize(3);

        DeliveryRouteRecord route0 = savedRecords.get(0);
        DeliveryRouteRecord route1 = savedRecords.get(1);
        DeliveryRouteRecord route2 = savedRecords.get(2);

        assertThat(route0.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(route0.getSequence()).isEqualTo(0);
        assertThat(route0.getOriginId()).isEqualTo(supplierId);
        assertThat(route0.getOriginType()).isEqualTo(RouteLocationType.COMPANY);
        assertThat(route0.getDestinationId()).isEqualTo(supplierHubId);
        assertThat(route0.getDestinationType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route0.getOriginLatitude()).isEqualTo(supplierCompany.latitude().doubleValue());
        assertThat(route0.getOriginLongitude()).isEqualTo(supplierCompany.longitude().doubleValue());
        assertThat(route0.getDestinationLatitude()).isEqualTo(originHub.latitude().doubleValue());
        assertThat(route0.getDestinationLongitude()).isEqualTo(originHub.longitude().doubleValue());
        assertThat(route0.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(route0.getExpectedDurationMin()).isEqualTo(0);
        assertThat(route0.getRemainDurationMin()).isEqualTo(60);

        assertThat(route1.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(route1.getSequence()).isEqualTo(1);
        assertThat(route1.getOriginId()).isEqualTo(supplierHubId);
        assertThat(route1.getOriginType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route1.getDestinationId()).isEqualTo(receiverHubId);
        assertThat(route1.getDestinationType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route1.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(route1.getExpectedDurationMin()).isEqualTo(60);
        assertThat(route1.getRemainDurationMin()).isEqualTo(60);

        assertThat(route2.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(route2.getSequence()).isEqualTo(2);
        assertThat(route2.getOriginId()).isEqualTo(receiverHubId);
        assertThat(route2.getOriginType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route2.getDestinationId()).isEqualTo(receiverId);
        assertThat(route2.getDestinationType()).isEqualTo(RouteLocationType.COMPANY);
        assertThat(route2.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(route2.getExpectedDurationMin()).isEqualTo(0);
        assertThat(route2.getRemainDurationMin()).isEqualTo(0);
    }

    @Test
    @DisplayName("user-service 조회 실패 시 예외 발생")
    void execute_fail_when_userService_fails() {
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "요청사항",
                "receiver_manager",
                desiredDeliveryAt
        );

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);
        when(userServiceClient.getUserByUsername("receiver_manager"))
                .thenThrow(new RuntimeException("user-service 호출 실패"));

        assertThrows(
                RuntimeException.class,
                () -> createDeliveryFromOrderService.execute(command)
        );

        verify(deliveryService, never()).createDelivery(any());
        verify(deliveryRouteRecordRepository, never()).saveAll(any());
        verify(deliveryPreparedEventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("이미 배송이 존재하면 중복 생성하지 않고 종료")
    void execute_skip_when_delivery_already_exists() {
        UUID orderId = UUID.randomUUID();

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "요청사항",
                "receiver_manager",
                LocalDateTime.of(2026, 4, 5, 15, 0)
        );

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(true);

        createDeliveryFromOrderService.execute(command);

        verify(deliveryRepository, times(1)).existsByOrderIdAndDeletedAtIsNull(orderId);
        verify(userServiceClient, never()).getUserByUsername(anyString());
        verify(companyServiceClient, never()).getCompany(any());
        verify(deliveryService, never()).createDelivery(any());
        verify(deliveryRouteRecordRepository, never()).saveAll(any());
        verify(deliveryEventProducer, never()).publishDeliveryCreated(any());
        verify(deliveryPreparedEventProducer, never()).publish(any());
    }

    @Test
    @DisplayName("배송 생성 완료 후 DeliveryCreatedEvent와 DeliveryPreparedEvent를 발행한다")
    void execute_publish_events() {
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID supplierHubId = UUID.randomUUID();
        UUID receiverHubId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "문 앞에 놔주세요",
                "receiver_manager",
                desiredDeliveryAt
        );

        HubResponse supplierHub = new HubResponse(
                supplierHubId, "경기 북부 센터", "허브 주소1", BigDecimal.ONE, BigDecimal.ONE
        );
        HubResponse receiverHub = new HubResponse(
                receiverHubId, "부산광역시 센터", "허브 주소2", BigDecimal.TEN, BigDecimal.TEN
        );
        HubPathResponseDto hubPath = new HubPathResponseDto(
                UUID.randomUUID(), supplierHubId, "경기 북부 센터", receiverHubId, "부산광역시 센터",
                null, null, null, null, null, null, BigDecimal.valueOf(10), 60,
                null, null, null, null, null, null
        );

        CommonResponse<HubResponse> supplierHubResponse = success(supplierHub);
        CommonResponse<HubResponse> receiverHubResponse = success(receiverHub);
        CommonResponse<HubPathResponseDto> hubPathResponse = success(hubPath);

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);
        when(userServiceClient.getUserByUsername("receiver_manager"))
                .thenReturn(new UserInfoDto(UUID.randomUUID(), "receiver_manager", "김담당", "kim@test.com", "U12345678", "COMPANY_MANAGER"));
        when(companyServiceClient.getCompany(supplierId))
                .thenReturn(new CompanyInfoDto(supplierId, supplierHubId, "공급업체 주소", BigDecimal.ONE, BigDecimal.ONE));
        when(companyServiceClient.getCompany(receiverId))
                .thenReturn(new CompanyInfoDto(receiverId, receiverHubId, "수령업체 주소", BigDecimal.TEN, BigDecimal.TEN));
        when(hubServiceClient.getHub(supplierHubId)).thenReturn(supplierHubResponse);
        when(hubServiceClient.getHub(receiverHubId)).thenReturn(receiverHubResponse);
        when(hubPathServiceClient.getHubPath("search", "경기 북부 센터", "부산광역시 센터"))
                .thenReturn(hubPathResponse);
        when(orderServiceClient.getOrder(orderId))
                .thenReturn(new OrderInfoDto(
                        orderId, productId, 50, BigDecimal.valueOf(12000), BigDecimal.valueOf(600000),
                        "CREATED", supplierId, receiverId, deliveryId, LocalDateTime.of(2026, 4, 5, 10, 0)
                ));
        when(productServiceClient.getProduct(productId))
                .thenReturn(new ProductInfoDto(
                        productId, supplierId, supplierHubId, "마른 오징어", BigDecimal.valueOf(12000),
                        100, true, LocalDateTime.now(), LocalDateTime.now()
                ));
        when(deliveryService.createDelivery(any()))
                .thenReturn(new CreateDeliveryResponseDto(deliveryId, orderId, DeliveryStatus.HUB_WAITING));
        when(deliveryRouteRecordRepository.saveAll(any())).thenAnswer(invocation -> {
            List<DeliveryRouteRecord> records = invocation.getArgument(0);
            assignRouteRecordIds(records);
            return records;
        });

        createDeliveryFromOrderService.execute(command);

        ArgumentCaptor<DeliveryCreatedEvent> createdCaptor = ArgumentCaptor.forClass(DeliveryCreatedEvent.class);
        verify(deliveryEventProducer, times(1)).publishDeliveryCreated(createdCaptor.capture());

        DeliveryCreatedEvent createdEvent = createdCaptor.getValue();
        assertThat(createdEvent.orderId()).isEqualTo(orderId);
        assertThat(createdEvent.deliveryId()).isEqualTo(deliveryId);

        ArgumentCaptor<DeliveryPreparedEvent> preparedCaptor = ArgumentCaptor.forClass(DeliveryPreparedEvent.class);
        verify(deliveryPreparedEventProducer, times(1)).publish(preparedCaptor.capture());

        DeliveryPreparedEvent preparedEvent = preparedCaptor.getValue();
        assertThat(preparedEvent.orderId()).isEqualTo(orderId);
        assertThat(preparedEvent.deliveryId()).isEqualTo(deliveryId);
        assertThat(preparedEvent.ordererName()).isEqualTo("김담당");
        assertThat(preparedEvent.ordererEmail()).isEqualTo("kim@test.com");
        assertThat(preparedEvent.productInfo()).isEqualTo("마른 오징어 50개");
        assertThat(preparedEvent.requirements()).isEqualTo("문 앞에 놔주세요");
        assertThat(preparedEvent.origin().name()).isEqualTo("부산광역시 센터");
        assertThat(preparedEvent.destination().address()).isEqualTo("수령업체 주소");
    }

    @Test
    @DisplayName("Delivery 생성 시 필요한 값들을 올바르게 전달한다")
    void execute_pass_correct_create_delivery_command() {
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID supplierHubId = UUID.randomUUID();
        UUID receiverHubId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "문 앞에 놔주세요",
                "receiver_manager",
                desiredDeliveryAt
        );

        HubResponse supplierHub = new HubResponse(
                supplierHubId, "경기 북부 센터", "허브 주소1", BigDecimal.ONE, BigDecimal.ONE
        );
        HubResponse receiverHub = new HubResponse(
                receiverHubId, "부산광역시 센터", "허브 주소2", BigDecimal.TEN, BigDecimal.TEN
        );
        HubPathResponseDto hubPath = new HubPathResponseDto(
                UUID.randomUUID(), supplierHubId, "경기 북부 센터", receiverHubId, "부산광역시 센터",
                null, null, null, null, null, null, BigDecimal.valueOf(10), 60,
                null, null, null, null, null, null
        );

        CommonResponse<HubResponse> supplierHubResponse = success(supplierHub);
        CommonResponse<HubResponse> receiverHubResponse = success(receiverHub);
        CommonResponse<HubPathResponseDto> hubPathResponse = success(hubPath);

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).thenReturn(false);
        when(userServiceClient.getUserByUsername("receiver_manager"))
                .thenReturn(new UserInfoDto(UUID.randomUUID(), "receiver_manager", "김담당", "kim@test.com", "U12345678", "COMPANY_MANAGER"));
        when(companyServiceClient.getCompany(supplierId))
                .thenReturn(new CompanyInfoDto(supplierId, supplierHubId, "공급업체 주소", BigDecimal.ONE, BigDecimal.ONE));
        when(companyServiceClient.getCompany(receiverId))
                .thenReturn(new CompanyInfoDto(receiverId, receiverHubId, "수령업체 주소", BigDecimal.TEN, BigDecimal.TEN));
        when(hubServiceClient.getHub(supplierHubId)).thenReturn(supplierHubResponse);
        when(hubServiceClient.getHub(receiverHubId)).thenReturn(receiverHubResponse);
        when(hubPathServiceClient.getHubPath("search", "경기 북부 센터", "부산광역시 센터"))
                .thenReturn(hubPathResponse);
        when(orderServiceClient.getOrder(orderId))
                .thenReturn(new OrderInfoDto(
                        orderId, productId, 50, BigDecimal.valueOf(12000), BigDecimal.valueOf(600000),
                        "CREATED", supplierId, receiverId, deliveryId, LocalDateTime.of(2026, 4, 5, 10, 0)
                ));
        when(productServiceClient.getProduct(productId))
                .thenReturn(new ProductInfoDto(
                        productId, supplierId, supplierHubId, "마른 오징어", BigDecimal.valueOf(12000),
                        100, true, LocalDateTime.now(), LocalDateTime.now()
                ));
        when(deliveryService.createDelivery(any()))
                .thenReturn(new CreateDeliveryResponseDto(deliveryId, orderId, DeliveryStatus.HUB_WAITING));
        when(deliveryRouteRecordRepository.saveAll(any())).thenAnswer(invocation -> {
            List<DeliveryRouteRecord> records = invocation.getArgument(0);
            assignRouteRecordIds(records);
            return records;
        });

        createDeliveryFromOrderService.execute(command);

        ArgumentCaptor<CreateDeliveryCommand> captor =
                ArgumentCaptor.forClass(CreateDeliveryCommand.class);

        verify(deliveryService).createDelivery(captor.capture());

        CreateDeliveryCommand createCommand = captor.getValue();

        assertThat(createCommand.orderId()).isEqualTo(orderId);
        assertThat(createCommand.originHubId()).isEqualTo(supplierHubId);
        assertThat(createCommand.destinationHubId()).isEqualTo(receiverHubId);
        assertThat(createCommand.deliveryAddress()).isEqualTo("수령업체 주소");
        assertThat(createCommand.receiverName()).isEqualTo("김담당");
        assertThat(createCommand.receiverSlackId()).isEqualTo("U12345678");
        assertThat(createCommand.requestNote()).isEqualTo("문 앞에 놔주세요");
        assertThat(createCommand.expectedDurationTotalMin()).isEqualTo(60);
        assertThat(createCommand.status()).isEqualTo(DeliveryStatus.HUB_WAITING);
        assertThat(createCommand.desiredDeliveryAt()).isEqualTo(desiredDeliveryAt);
    }

    @SuppressWarnings("unchecked")
    private <T> CommonResponse<T> success(T data) {
        CommonResponse<T> response = mock(CommonResponse.class);
        when(response.getData()).thenReturn(data);
        return response;
    }

    private void assignRouteRecordIds(List<DeliveryRouteRecord> records) {
        try {
            var idField = DeliveryRouteRecord.class.getDeclaredField("deliveryRouteRecordId");
            idField.setAccessible(true);

            for (DeliveryRouteRecord record : records) {
                if (idField.get(record) == null) {
                    idField.set(record, UUID.randomUUID());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}