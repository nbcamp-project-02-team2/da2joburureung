package com.da2jobu.deliveryservice.application.deliveryManager.scheduler;

import com.da2jobu.deliveryservice.application.deliveryManager.service.CompanyDeliveryAssignmentService;
import com.da2jobu.deliveryservice.infrastructure.client.HubServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.HubListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyDeliveryAssignmentScheduler {

    private final CompanyDeliveryAssignmentService companyDeliveryAssignmentService;
    private final HubServiceClient hubServiceClient;


    @Scheduled(cron = "0 0 6 * * *")
    public void scheduleDailyAssignment() {
        log.info("업체 배송 일일 배정 스케줄러 시작");

        //hub-service에서 허브 목록 조회
        List<HubListResponse> hubs = hubServiceClient.getHubs(50).getData().content();

        int successCount = 0;
        int failCount = 0;

        for (HubListResponse hub : hubs) {
            try {
                companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hub.hubId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("업체 배송 담당자 배송 일괄 배정 실패 - hubId={}, error={}", hub.hubId(), e.getMessage(), e);
            }
        }

        log.info("업체 배송 담당자 배송 일일 배정 스케줄러 완료 - 성공={}, 실패={}", successCount, failCount);
    }
}

