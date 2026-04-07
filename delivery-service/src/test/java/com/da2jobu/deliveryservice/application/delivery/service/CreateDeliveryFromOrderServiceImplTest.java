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
import org.junit.jupiter.api.Nested;
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

    @Nested
    class ExecuteSuccessTests {

        @Test
        @DisplayName("주문 이벤트 기반으로 Delivery 생성 및 DeliveryRouteRecord 4건 생성")
        void execute_success() {
            // given
            TestFixture fixture = TestFixture.create();
            stubCommonSuccessFlow(fixture);

            // when
            createDeliveryFromOrderService.execute(fixture.command);

            // then
            verify(userServiceClient, times(1)).getUserByUsername("receiver_manager");
            verify(companyServiceClient, times(1)).getCompany(fixture.supplierId);
            verify(companyServiceClient, times(1)).getCompany(fixture.receiverId);
            verify(hubPathServiceClient, times(1))
                    .getHubPath("경기 북부 센터", "부산광역시 센터");
            verify(orderServiceClient, times(1)).getOrder(fixture.orderId);
            verify(productServiceClient, times(1)).getProduct(fixture.productId);
            verify(deliveryService, times(1)).createDelivery(any());
            verify(deliveryEventProducer, times(1)).publishDeliveryCreated(any());
            verify(deliveryPreparedEventProducer, times(1)).publish(any());
            verify(hubDeliveryAssignmentService, times(1))
                    .assignHubDelivery(any(), any(), eq(fixture.supplierHubId));

            ArgumentCaptor<List<DeliveryRouteRecord>> routeRecordCaptor = ArgumentCaptor.forClass(List.class);
            verify(deliveryRouteRecordRepository, times(1)).saveAll(routeRecordCaptor.capture());

            List<DeliveryRouteRecord> savedRecords = routeRecordCaptor.getValue();
            assertThat(savedRecords).hasSize(4);

            DeliveryRouteRecord route0 = savedRecords.get(0);
            DeliveryRouteRecord route1 = savedRecords.get(1);
            DeliveryRouteRecord route2 = savedRecords.get(2);
            DeliveryRouteRecord route3 = savedRecords.get(3);

            assertThat(route0.getDeliveryId()).isEqualTo(fixture.deliveryId);
            assertThat(route0.getSequence()).isEqualTo(0);
            assertThat(route0.getOriginId()).isEqualTo(fixture.supplierId);
            assertThat(route0.getOriginType()).isEqualTo(RouteLocationType.COMPANY);
            assertThat(route0.getDestinationId()).isEqualTo(fixture.supplierHubId);
            assertThat(route0.getDestinationType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route0.getOriginLatitude()).isEqualTo(fixture.supplierCompany.latitude().doubleValue());
            assertThat(route0.getOriginLongitude()).isEqualTo(fixture.supplierCompany.longitude().doubleValue());
            assertThat(route0.getDestinationLatitude()).isEqualTo(fixture.originHub.latitude().doubleValue());
            assertThat(route0.getDestinationLongitude()).isEqualTo(fixture.originHub.longitude().doubleValue());
            assertThat(route0.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(route0.getExpectedDurationMin()).isEqualTo(0);
            assertThat(route0.getRemainDurationMin()).isEqualTo(289);

            assertThat(route1.getDeliveryId()).isEqualTo(fixture.deliveryId);
            assertThat(route1.getSequence()).isEqualTo(1);
            assertThat(route1.getOriginId()).isEqualTo(fixture.supplierHubId);
            assertThat(route1.getOriginType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route1.getDestinationId()).isEqualTo(fixture.middleHubId);
            assertThat(route1.getDestinationType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route1.getExpectedDistanceKm()).isEqualByComparingTo(new BigDecimal("205.82"));
            assertThat(route1.getExpectedDurationMin()).isEqualTo(143);
            assertThat(route1.getRemainDurationMin()).isEqualTo(289);

            assertThat(route2.getDeliveryId()).isEqualTo(fixture.deliveryId);
            assertThat(route2.getSequence()).isEqualTo(2);
            assertThat(route2.getOriginId()).isEqualTo(fixture.middleHubId);
            assertThat(route2.getOriginType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route2.getDestinationId()).isEqualTo(fixture.receiverHubId);
            assertThat(route2.getDestinationType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route2.getExpectedDistanceKm()).isEqualByComparingTo(new BigDecimal("219.40"));
            assertThat(route2.getExpectedDurationMin()).isEqualTo(146);
            assertThat(route2.getRemainDurationMin()).isEqualTo(146);

            assertThat(route3.getDeliveryId()).isEqualTo(fixture.deliveryId);
            assertThat(route3.getSequence()).isEqualTo(3);
            assertThat(route3.getOriginId()).isEqualTo(fixture.receiverHubId);
            assertThat(route3.getOriginType()).isEqualTo(RouteLocationType.HUB);
            assertThat(route3.getDestinationId()).isEqualTo(fixture.receiverId);
            assertThat(route3.getDestinationType()).isEqualTo(RouteLocationType.COMPANY);
            assertThat(route3.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(route3.getExpectedDurationMin()).isEqualTo(0);
            assertThat(route3.getRemainDurationMin()).isEqualTo(0);
        }

        @Test
        @DisplayName("배송 생성 완료 후 DeliveryCreatedEvent와 DeliveryPreparedEvent를 발행한다")
        void execute_publish_events() {
            // given
            TestFixture fixture = TestFixture.create();
            stubCommonSuccessFlow(fixture);

            // when
            createDeliveryFromOrderService.execute(fixture.command);

            // then
            ArgumentCaptor<DeliveryCreatedEvent> createdCaptor = ArgumentCaptor.forClass(DeliveryCreatedEvent.class);
            verify(deliveryEventProducer, times(1)).publishDeliveryCreated(createdCaptor.capture());

            DeliveryCreatedEvent createdEvent = createdCaptor.getValue();
            assertThat(createdEvent.orderId()).isEqualTo(fixture.orderId);
            assertThat(createdEvent.deliveryId()).isEqualTo(fixture.deliveryId);

            ArgumentCaptor<DeliveryPreparedEvent> preparedCaptor = ArgumentCaptor.forClass(DeliveryPreparedEvent.class);
            verify(deliveryPreparedEventProducer, times(1)).publish(preparedCaptor.capture());

            DeliveryPreparedEvent preparedEvent = preparedCaptor.getValue();
            assertThat(preparedEvent.orderId()).isEqualTo(fixture.orderId);
            assertThat(preparedEvent.deliveryId()).isEqualTo(fixture.deliveryId);
            assertThat(preparedEvent.ordererName()).isEqualTo("김담당");
            assertThat(preparedEvent.ordererEmail()).isEqualTo("kim@test.com");
            assertThat(preparedEvent.productInfo()).isEqualTo("마른 오징어 50개");
            assertThat(preparedEvent.requirements()).isEqualTo("문 앞에 놔주세요");
            assertThat(preparedEvent.origin().name()).isEqualTo("부산광역시 센터");
            assertThat(preparedEvent.destination().address()).isEqualTo("수령업체 주소");
            assertThat(preparedEvent.waypoints()).hasSize(2);
            assertThat(preparedEvent.waypoints().get(0).name()).isEqualTo("경상북도 센터");
            assertThat(preparedEvent.waypoints().get(1).name()).isEqualTo("부산광역시 센터");
        }

        @Test
        @DisplayName("Delivery 생성 시 필요한 값들을 올바르게 전달한다")
        void execute_pass_correct_create_delivery_command() {
            // given
            TestFixture fixture = TestFixture.create();
            stubCommonSuccessFlow(fixture);

            // when
            createDeliveryFromOrderService.execute(fixture.command);

            // then
            ArgumentCaptor<CreateDeliveryCommand> captor =
                    ArgumentCaptor.forClass(CreateDeliveryCommand.class);

            verify(deliveryService).createDelivery(captor.capture());

            CreateDeliveryCommand createCommand = captor.getValue();

            assertThat(createCommand.orderId()).isEqualTo(fixture.orderId);
            assertThat(createCommand.originHubId()).isEqualTo(fixture.supplierHubId);
            assertThat(createCommand.destinationHubId()).isEqualTo(fixture.receiverHubId);
            assertThat(createCommand.deliveryAddress()).isEqualTo("수령업체 주소");
            assertThat(createCommand.receiverName()).isEqualTo("김담당");
            assertThat(createCommand.receiverSlackId()).isEqualTo("U12345678");
            assertThat(createCommand.supplierCompanyId()).isEqualTo(fixture.supplierId);
            assertThat(createCommand.receiverCompanyId()).isEqualTo(fixture.receiverId);
            assertThat(createCommand.requestNote()).isEqualTo("문 앞에 놔주세요");
            assertThat(createCommand.expectedDurationTotalMin()).isEqualTo(289);
            assertThat(createCommand.status()).isEqualTo(DeliveryStatus.HUB_WAITING);
            assertThat(createCommand.desiredDeliveryAt()).isEqualTo(fixture.desiredDeliveryAt);
        }
    }

    @Nested
    class ExecuteFailureTests {

        @Test
        @DisplayName("user-service 조회 실패 시 예외 발생")
        void execute_fail_when_userService_fails() {
            // given
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

            // when
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> createDeliveryFromOrderService.execute(command)
            );

            // then
            assertThat(exception.getMessage()).isEqualTo("user-service 호출 실패");
            verify(deliveryService, never()).createDelivery(any());
            verify(deliveryRouteRecordRepository, never()).saveAll(any());
            verify(deliveryPreparedEventProducer, never()).publish(any());
        }

        @Test
        @DisplayName("이미 배송이 존재하면 중복 생성하지 않고 종료")
        void execute_skip_when_delivery_already_exists() {
            // given
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

            // when
            createDeliveryFromOrderService.execute(command);

            // then
            verify(deliveryRepository, times(1)).existsByOrderIdAndDeletedAtIsNull(orderId);
            verify(userServiceClient, never()).getUserByUsername(anyString());
            verify(companyServiceClient, never()).getCompany(any());
            verify(deliveryService, never()).createDelivery(any());
            verify(deliveryRouteRecordRepository, never()).saveAll(any());
            verify(deliveryEventProducer, never()).publishDeliveryCreated(any());
            verify(deliveryPreparedEventProducer, never()).publish(any());
        }
    }

    private void stubCommonSuccessFlow(TestFixture fixture) {
        CommonResponse<HubResponse> originHubResponse = success(fixture.originHub);
        CommonResponse<HubResponse> middleHubResponse = success(fixture.middleHub);
        CommonResponse<HubResponse> destinationHubResponse = success(fixture.destinationHub);
        CommonResponse<HubPathResponseDto> hubPathResponse = success(fixture.hubPath);

        when(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(fixture.orderId)).thenReturn(false);
        //when(userServiceClient.getUserByUsername("receiver_manager")).thenReturn(fixture.userInfoDto);
        when(companyServiceClient.getCompany(fixture.supplierId)).thenReturn(fixture.supplierCompany);
        when(companyServiceClient.getCompany(fixture.receiverId)).thenReturn(fixture.receiverCompany);

        when(hubServiceClient.getHub(fixture.supplierHubId)).thenReturn(originHubResponse);
        when(hubServiceClient.getHub(fixture.middleHubId)).thenReturn(middleHubResponse);
        when(hubServiceClient.getHub(fixture.receiverHubId)).thenReturn(destinationHubResponse);

        when(hubPathServiceClient.getHubPath("경기 북부 센터", "부산광역시 센터"))
                .thenReturn(hubPathResponse);

        when(orderServiceClient.getOrder(fixture.orderId)).thenReturn(fixture.orderInfoDto);
        when(productServiceClient.getProduct(fixture.productId)).thenReturn(fixture.productInfoDto);
        when(deliveryService.createDelivery(any())).thenReturn(fixture.createDeliveryResponseDto);

        when(deliveryRouteRecordRepository.saveAll(any())).thenAnswer(invocation -> {
            List<DeliveryRouteRecord> records = invocation.getArgument(0);
            assignRouteRecordIds(records);
            return records;
        });
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

    private static class TestFixture {
        private final UUID orderId = UUID.randomUUID();
        private final UUID supplierId = UUID.randomUUID();
        private final UUID receiverId = UUID.randomUUID();
        private final UUID supplierHubId = UUID.randomUUID();
        private final UUID middleHubId = UUID.randomUUID();
        private final UUID receiverHubId = UUID.randomUUID();
        private final UUID deliveryId = UUID.randomUUID();
        private final UUID productId = UUID.randomUUID();
        private final LocalDateTime desiredDeliveryAt = LocalDateTime.of(2026, 4, 5, 15, 0);

        private final CreateDeliveryFromOrderCommand command =
                new CreateDeliveryFromOrderCommand(
                        orderId,
                        supplierId,
                        receiverId,
                        "문 앞에 놔주세요",
                        "receiver_manager",
                        desiredDeliveryAt
                );

        private final UserInfoDto userInfoDto =
                new UserInfoDto(
                        UUID.randomUUID(),
                        "receiver_manager",
                        "김담당",
                        "kim@test.com",
                        "U12345678",
                        "COMPANY_MANAGER"
                );

        private final CompanyInfoDto supplierCompany =
                new CompanyInfoDto(
                        supplierId,
                        supplierHubId,
                        "서울특별시 공급업체 주소",
                        BigDecimal.valueOf(37.123456),
                        BigDecimal.valueOf(127.123456)
                );

        private final CompanyInfoDto receiverCompany =
                new CompanyInfoDto(
                        receiverId,
                        receiverHubId,
                        "수령업체 주소",
                        BigDecimal.valueOf(35.654321),
                        BigDecimal.valueOf(129.654321)
                );

        private final HubResponse originHub =
                new HubResponse(
                        supplierHubId,
                        "경기 북부 센터",
                        "서울특별시 허브 주소",
                        BigDecimal.valueOf(37.500000),
                        BigDecimal.valueOf(127.500000)
                );

        private final HubResponse middleHub =
                new HubResponse(
                        middleHubId,
                        "경상북도 센터",
                        "경상북도 허브 주소",
                        BigDecimal.valueOf(36.100000),
                        BigDecimal.valueOf(128.100000)
                );

        private final HubResponse destinationHub =
                new HubResponse(
                        receiverHubId,
                        "부산광역시 센터",
                        "부산광역시 허브 주소",
                        BigDecimal.valueOf(35.100000),
                        BigDecimal.valueOf(129.100000)
                );

        private final List<HubPathResponseDto.StepDto> steps =
                List.of(
                        new HubPathResponseDto.StepDto(
                                1,
                                supplierHubId,
                                "경기 북부 센터",
                                middleHubId,
                                "경상북도 센터",
                                new BigDecimal("205.82"),
                                143
                        ),
                        new HubPathResponseDto.StepDto(
                                2,
                                middleHubId,
                                "경상북도 센터",
                                receiverHubId,
                                "부산광역시 센터",
                                new BigDecimal("219.40"),
                                146
                        )
                );

        private final HubPathResponseDto hubPath =
                new HubPathResponseDto(
                        UUID.randomUUID(),
                        supplierHubId,
                        "경기 북부 센터",
                        receiverHubId,
                        "부산광역시 센터",
                        steps,
                        new BigDecimal("425.22"),
                        289,
                        "system",
                        LocalDateTime.of(2026, 4, 6, 12, 31, 52),
                        "system",
                        LocalDateTime.of(2026, 4, 6, 12, 31, 52)
                );

        private final OrderInfoDto orderInfoDto =
                new OrderInfoDto(
                        orderId,
                        productId,
                        50,
                        BigDecimal.valueOf(12000),
                        BigDecimal.valueOf(600000),
                        "CREATED",
                        supplierId,
                        receiverId,
                        deliveryId,
                        LocalDateTime.of(2026, 4, 5, 10, 0)
                );

        private final ProductInfoDto productInfoDto =
                new ProductInfoDto(
                        productId,
                        supplierId,
                        supplierHubId,
                        "마른 오징어",
                        BigDecimal.valueOf(12000),
                        100,
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        private final CreateDeliveryResponseDto createDeliveryResponseDto =
                new CreateDeliveryResponseDto(
                        deliveryId,
                        orderId,
                        DeliveryStatus.HUB_WAITING
                );

        static TestFixture create() {
            return new TestFixture();
        }
    }
}