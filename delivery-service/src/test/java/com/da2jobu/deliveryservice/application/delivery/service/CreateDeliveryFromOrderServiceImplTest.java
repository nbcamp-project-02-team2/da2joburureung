package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import com.da2jobu.deliveryservice.infrastructure.client.CompanyServiceClient;
import com.da2jobu.deliveryservice.infrastructure.client.UserServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.CompanyInfoDto;
import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateDeliveryFromOrderServiceImplTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CompanyServiceClient companyServiceClient;

    @InjectMocks
    private CreateDeliveryFromOrderServiceImpl createDeliveryFromOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("주문 이벤트 기반으로 Delivery 생성 및 DeliveryRouteRecord 2건 생성")
    void execute_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID supplierHubId = UUID.randomUUID();
        UUID receiverHubId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "문 앞에 놔주세요",
                "receiver_manager"
        );

        UserInfoDto userInfoDto = new UserInfoDto(
                UUID.randomUUID(),
                "receiver_manager",
                "김담당",
                "U12345678"
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

        CreateDeliveryResponseDto createDeliveryResponseDto = new CreateDeliveryResponseDto(
                deliveryId,
                orderId,
                DeliveryStatus.HUB_WAITING
        );

        when(userServiceClient.getUserByUsername("receiver_manager")).thenReturn(userInfoDto);
        when(companyServiceClient.getCompany(supplierId)).thenReturn(supplierCompany);
        when(companyServiceClient.getCompany(receiverId)).thenReturn(receiverCompany);
        when(deliveryService.createDelivery(any())).thenReturn(createDeliveryResponseDto);

        // when
        createDeliveryFromOrderService.execute(command);

        // then
        verify(userServiceClient, times(1)).getUserByUsername("receiver_manager");
        verify(companyServiceClient, times(1)).getCompany(supplierId);
        verify(companyServiceClient, times(1)).getCompany(receiverId);
        verify(deliveryService, times(1)).createDelivery(any());

        ArgumentCaptor<List<DeliveryRouteRecord>> routeRecordCaptor = ArgumentCaptor.forClass(List.class);
        verify(deliveryRouteRecordRepository, times(1)).saveAll(routeRecordCaptor.capture());

        List<DeliveryRouteRecord> savedRecords = routeRecordCaptor.getValue();

        assertThat(savedRecords).hasSize(2);

        DeliveryRouteRecord route0 = savedRecords.get(0);
        DeliveryRouteRecord route1 = savedRecords.get(1);

        assertThat(route0.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(route0.getSequence()).isEqualTo(0);
        assertThat(route0.getOriginId()).isEqualTo(supplierHubId);
        assertThat(route0.getOriginType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route0.getDestinationId()).isEqualTo(receiverHubId);
        assertThat(route0.getDestinationType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route0.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(route0.getExpectedDurationMin()).isEqualTo(60);
        assertThat(route0.getRemainDurationMin()).isEqualTo(90);

        assertThat(route1.getDeliveryId()).isEqualTo(deliveryId);
        assertThat(route1.getSequence()).isEqualTo(1);
        assertThat(route1.getOriginId()).isEqualTo(receiverHubId);
        assertThat(route1.getOriginType()).isEqualTo(RouteLocationType.HUB);
        assertThat(route1.getDestinationId()).isEqualTo(receiverId);
        assertThat(route1.getDestinationType()).isEqualTo(RouteLocationType.COMPANY);
        assertThat(route1.getExpectedDistanceKm()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        assertThat(route1.getExpectedDurationMin()).isEqualTo(30);
        assertThat(route1.getRemainDurationMin()).isEqualTo(30);
    }

    @Test
    @DisplayName("user-service 조회 실패 시 예외 발생")
    void execute_fail_when_userService_fails() {
        UUID orderId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        CreateDeliveryFromOrderCommand command = new CreateDeliveryFromOrderCommand(
                orderId,
                supplierId,
                receiverId,
                "요청사항",
                "receiver_manager"
        );

        when(userServiceClient.getUserByUsername("receiver_manager"))
                .thenThrow(new RuntimeException("user-service 호출 실패"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> createDeliveryFromOrderService.execute(command)
        );

        verify(deliveryService, never()).createDelivery(any());
        verify(deliveryRouteRecordRepository, never()).saveAll(any());
    }
}