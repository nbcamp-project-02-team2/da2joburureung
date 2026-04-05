package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.repository.DeliveryRepository;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import common.config.JpaAuditingConfig;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Import(JpaAuditingConfig.class)
class DeliveryRouteRecordRepositoryTest {

    @Autowired
    private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    @DisplayName("허브별 당일 업체배송 대상만 조회한다")
    void findTodayCompanyDeliveryRoutesByHubId_success() {
        // given
        UUID hubId = UUID.randomUUID();
        UUID destinationCompanyId = UUID.randomUUID();

        LocalDate today = LocalDate.now();
        LocalDateTime desiredDeliveryAt = today.atTime(15, 0);

        Delivery delivery = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(DeliveryStatus.ARRIVED_AT_DESTINATION_HUB)
                .originHubId(UUID.randomUUID())
                .destinationHubId(hubId)
                .deliveryAddress("서울시")
                .receiverName("홍길동")
                .receiverSlackId("U123")
                .companyDeliveryManagerId(null)
                .requestNote("문 앞")
                .expectedDurationTotalMin(30)
                .desiredDeliveryAt(desiredDeliveryAt)
                .startedAt(null)
                .completedAt(null)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryRouteRecord routeRecord = DeliveryRouteRecord.builder()
                .deliveryId(savedDelivery.getDeliveryId())
                .sequence(1)
                .originId(hubId)
                .originType(RouteLocationType.HUB)
                .destinationId(destinationCompanyId)
                .destinationType(RouteLocationType.COMPANY)
                .expectedDistanceKm(BigDecimal.valueOf(5.0))
                .expectedDurationMin(30)
                .realDistanceKm(null)
                .realDurationMin(null)
                .remainDurationMin(30)
                .status(DeliveryRouteStatus.ARRIVED_AT_DESTINATION_HUB)
                .deliveryManagerId(null)
                .build();

        deliveryRouteRecordRepository.save(routeRecord);

        // when
        List<TodayCompanyDeliveryRouteResponseDto> result =
                deliveryRouteRecordRepository.findTodayCompanyDeliveryRoutesByHubId(
                        hubId,
                        DeliveryStatus.ARRIVED_AT_DESTINATION_HUB,
                        RouteLocationType.HUB,
                        RouteLocationType.COMPANY,
                        today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay()
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).originId()).isEqualTo(hubId);
        assertThat(result.get(0).destinationType()).isEqualTo(RouteLocationType.COMPANY);
        assertThat(result.get(0).desiredDeliveryAt()).isEqualTo(desiredDeliveryAt);
    }
}