package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GetTodayCompanyDeliveryRoutesServiceImplTest {

    @Mock
    private DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @InjectMocks
    private GetTodayCompanyDeliveryRoutesServiceImpl getTodayCompanyDeliveryRoutesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("허브별 당일 업체배송 대상 조회 성공")
    void getTodayCompanyDeliveryRoutes_success() {
        // given
        UUID hubId = UUID.randomUUID();

        TodayCompanyDeliveryRouteResponseDto dto = new TodayCompanyDeliveryRouteResponseDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                hubId,
                RouteLocationType.HUB,
                UUID.randomUUID(),
                RouteLocationType.COMPANY,
                BigDecimal.valueOf(5.0),
                30,
                null,
                null,
                30,
                DeliveryRouteStatus.ARRIVED_AT_DESTINATION_HUB,
                null,
                LocalDateTime.of(2026, 4, 5, 15, 0)
        );

        when(deliveryRouteRecordRepository.findTodayCompanyDeliveryRoutesByHubId(
                eq(hubId),
                eq(DeliveryStatus.ARRIVED_AT_DESTINATION_HUB),
                eq(RouteLocationType.HUB),
                eq(RouteLocationType.COMPANY),
                any(),
                any()
        )).thenReturn(List.of(dto));

        // when
        List<TodayCompanyDeliveryRouteResponseDto> result =
                getTodayCompanyDeliveryRoutesService.getTodayCompanyDeliveryRoutes(hubId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).originId()).isEqualTo(hubId);
        assertThat(result.get(0).originType()).isEqualTo(RouteLocationType.HUB);
        assertThat(result.get(0).destinationType()).isEqualTo(RouteLocationType.COMPANY);
    }
}