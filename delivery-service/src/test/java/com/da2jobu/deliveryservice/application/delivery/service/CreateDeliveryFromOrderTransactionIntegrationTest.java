package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryFromOrderCommand;
import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 실제 Spring 트랜잭션 프록시와 H2 기반 JPA 저장소를 사용해 트랜잭션 동작을 검증합니다.
 * Feign 클라이언트와 이벤트 발행기는 Mock으로 대체하므로 Kafka 메시지 전달 자체는 검증하지 않습니다.
 * 서비스의 커밋 및 롤백 경계를 확인하기 위해 테스트 클래스와 메서드에는 @Transactional을 적용하지 않습니다.
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.kafka.listener.auto-startup=false"
})
@ActiveProfiles("test")
class CreateDeliveryFromOrderTransactionIntegrationTest {

    @Autowired private CreateDeliveryFromOrderService createDeliveryFromOrderService;
    @Autowired private DeliveryRepository deliveryRepository;
    @MockitoSpyBean private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @MockitoBean private UserServiceClient userServiceClient;
    @MockitoBean private CompanyServiceClient companyServiceClient;
    @MockitoBean private HubServiceClient hubServiceClient;
    @MockitoBean private HubPathServiceClient hubPathServiceClient;
    @MockitoBean private OrderServiceClient orderServiceClient;
    @MockitoBean private ProductServiceClient productServiceClient;
    @MockitoBean private DeliveryEventProducer deliveryEventProducer;
    @MockitoBean private DeliveryPreparedEventProducer deliveryPreparedEventProducer;

    private final UUID orderId = UUID.randomUUID();
    private final UUID supplierId = UUID.randomUUID();
    private final UUID receiverId = UUID.randomUUID();
    private final UUID originHubId = UUID.randomUUID();
    private final UUID middleHubId = UUID.randomUUID();
    private final UUID destinationHubId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void prepareFeignResponses() {
        deliveryRouteRecordRepository.deleteAll();
        deliveryRepository.deleteAll();
        LocalDateTime now = LocalDateTime.of(2026, 4, 5, 10, 0);

        UserInfoDto user = new UserInfoDto(
                UUID.randomUUID(), "receiver_manager", "김담당",
                "kim@test.com", "U12345678", "COMPANY_MANAGER");
        when(userServiceClient.getUserByUsername("receiver_manager"))
                .thenReturn(response(user));

        when(companyServiceClient.getCompany(supplierId))
                .thenReturn(new CompanyInfoDto(supplierId, originHubId, "공급업체 주소",
                        new BigDecimal("37.123456"), new BigDecimal("127.123456")));
        when(companyServiceClient.getCompany(receiverId))
                .thenReturn(new CompanyInfoDto(receiverId, destinationHubId, "수령업체 주소",
                        new BigDecimal("35.654321"), new BigDecimal("129.654321")));

        HubResponse origin = hub(originHubId, "경기 북부 센터", "37.500000", "127.500000");
        HubResponse middle = hub(middleHubId, "경상북도 센터", "36.100000", "128.100000");
        HubResponse destination = hub(destinationHubId, "부산광역시 센터", "35.100000", "129.100000");
        when(hubServiceClient.getHub(originHubId)).thenReturn(response(origin));
        when(hubServiceClient.getHub(middleHubId)).thenReturn(response(middle));
        when(hubServiceClient.getHub(destinationHubId)).thenReturn(response(destination));

        HubPathResponseDto path = new HubPathResponseDto(
                UUID.randomUUID(), originHubId, "경기 북부 센터",
                destinationHubId, "부산광역시 센터",
                List.of(
                        new HubPathResponseDto.StepDto(1, originHubId, "경기 북부 센터",
                                middleHubId, "경상북도 센터", new BigDecimal("205.82"), 143),
                        new HubPathResponseDto.StepDto(2, middleHubId, "경상북도 센터",
                                destinationHubId, "부산광역시 센터", new BigDecimal("219.40"), 146)
                ), new BigDecimal("425.22"), 289,
                "system", now, "system", now);
        when(hubPathServiceClient.getHubPath("경기 북부 센터", "부산광역시 센터"))
                .thenReturn(response(path));

        OrderInfoDto order = new OrderInfoDto(
                orderId, productId, 50, new BigDecimal("12000"), "CREATED",
                supplierId, receiverId, null, destinationHubId, "문 앞에 놔주세요",
                now.plusDays(2), now, now);
        when(orderServiceClient.getOrder(orderId)).thenReturn(response(order));
        when(productServiceClient.getProduct(productId)).thenReturn(new ProductInfoDto(
                productId, supplierId, originHubId, "마른 오징어",
                new BigDecimal("12000"), 100, true, now, now));
    }

    @Test
    @DisplayName("정상 처리 시 Delivery 1건과 RouteRecord 4건이 DB에 커밋된다")
    void commitsDeliveryAndFourRouteRecords() {
        createDeliveryFromOrderService.execute(command());

        List<Delivery> deliveries = deliveryRepository.findAll().stream()
                .filter(delivery -> orderId.equals(delivery.getOrderId())).toList();
        assertThat(deliveries).hasSize(1);

        UUID deliveryId = deliveries.get(0).getDeliveryId();
        List<DeliveryRouteRecord> records = deliveryRouteRecordRepository
                .findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(deliveryId);
        assertThat(records).hasSize(4);
        assertThat(records).extracting(DeliveryRouteRecord::getSequence)
                .containsExactly(0, 1, 2, 3);
        verify(deliveryEventProducer).publishDeliveryCreated(any());
        verify(deliveryPreparedEventProducer).publish(any());
    }

    @Test
    @DisplayName("RouteRecord 저장에 실패하면 앞서 저장한 Delivery도 롤백된다")
    void rollsBackDeliveryWhenRouteRecordSaveFails() {
        doThrow(new IllegalStateException("forced route record failure"))
                .when(deliveryRouteRecordRepository).saveAll(any());

        assertThatThrownBy(() -> createDeliveryFromOrderService.execute(command()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forced route record failure");

        assertThat(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).isFalse();
        assertThat(deliveryRepository.findAll().stream()
                .filter(d -> orderId.equals(d.getOrderId()))).isEmpty();
        assertThat(deliveryRouteRecordRepository.count()).isZero();
        verify(deliveryRouteRecordRepository).saveAll(any());
        verifyNoInteractions(deliveryEventProducer, deliveryPreparedEventProducer);
    }

    @Test
    @DisplayName("DB 저장 후 후속 Feign 호출이 실패해도 Delivery와 RouteRecord가 롤백되고 이벤트는 발행되지 않는다")
    void rollsBackBothWhenPostSaveFeignFails() {
        when(orderServiceClient.getOrder(orderId))
                .thenThrow(new IllegalStateException("forced order lookup failure"));

        assertThatThrownBy(() -> createDeliveryFromOrderService.execute(command()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forced order lookup failure");

        assertThat(deliveryRepository.existsByOrderIdAndDeletedAtIsNull(orderId)).isFalse();
        assertThat(deliveryRouteRecordRepository.count()).isZero();
        verify(deliveryRouteRecordRepository).saveAll(any());
        verifyNoInteractions(deliveryEventProducer, deliveryPreparedEventProducer);
    }

    private CreateDeliveryFromOrderCommand command() {
        return new CreateDeliveryFromOrderCommand(
                orderId, supplierId, receiverId,
                "문 앞에 놔주세요", "receiver_manager",
                LocalDateTime.of(2026, 4, 7, 15, 0));
    }

    private HubResponse hub(UUID id, String name, String latitude, String longitude) {
        return new HubResponse(id, name, name + " 주소",
                new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private <T> CommonResponse<T> response(T data) {
        // 중첩 Mockito 스터빙을 피하기 위해 실제 응답 객체를 생성합니다.
        return CommonResponse.ok(data).getBody();
    }
}
