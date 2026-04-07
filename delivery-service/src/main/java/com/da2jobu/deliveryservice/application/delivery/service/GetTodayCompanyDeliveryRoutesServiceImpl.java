package com.da2jobu.deliveryservice.application.delivery.service;

import com.da2jobu.deliveryservice.application.delivery.dto.TodayCompanyDeliveryRouteResponseDto;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.repository.DeliveryRouteRecordRepository;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTodayCompanyDeliveryRoutesServiceImpl implements GetTodayCompanyDeliveryRoutesService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final DeliveryRouteRecordRepository deliveryRouteRecordRepository;

    @Override
    public List<TodayCompanyDeliveryRouteResponseDto> getTodayCompanyDeliveryRoutes(UUID hubId) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        LocalDateTime todayStart = today.atTime(6, 0);
        LocalDateTime tomorrowStart = today.plusDays(1).atTime(6, 0);

        log.info("허브별 당일 업체배송 대상 조회 시작 - hubId={}, todayStart={}, tomorrowStart={}",
                hubId, todayStart, tomorrowStart);

        List<TodayCompanyDeliveryRouteResponseDto> result =
                deliveryRouteRecordRepository.findTodayCompanyDeliveryRoutesByHubId(
                        hubId,
                        DeliveryStatus.ARRIVED_AT_DESTINATION_HUB,
                        RouteLocationType.HUB,
                        RouteLocationType.COMPANY,
                        todayStart,
                        tomorrowStart
                );

        log.info("허브별 당일 업체배송 대상 조회 완료 - hubId={}, count={}", hubId, result.size());

        return result;
    }
}
