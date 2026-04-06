package com.da2jobu.deliveryservice.application.deliveryManager.scheduler;

import com.da2jobu.deliveryservice.application.deliveryManager.service.CompanyDeliveryAssignmentService;
import com.da2jobu.deliveryservice.infrastructure.delivery.client.HubServiceClient;
import com.da2jobu.deliveryservice.infrastructure.dto.HubListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyDeliveryAssignmentScheduler {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL_MS = 10_000L;

    private final CompanyDeliveryAssignmentService companyDeliveryAssignmentService;
    private final HubServiceClient hubServiceClient;

    @Scheduled(cron = "0 0 6 * * *")
    public void scheduleDailyAssignment() {
        log.info("업체 배송 일일 배정 스케줄러 시작");

        //hub-service에서 허브 목록 조회
        List<HubListResponse> hubs = hubServiceClient.getHubs(50).getData().content();

        List<UUID> failedHubIds = new ArrayList<>();
        int successCount = 0;

        for (HubListResponse hub : hubs) {
            try {
                companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hub.hubId());
                successCount++;
            } catch (Exception e) {
                failedHubIds.add(hub.hubId());
                log.error("업체 배송 담당자 배송 일괄 배정 실패 - hubId={}, error={}", hub.hubId(), e.getMessage(), e);
            }
        }

        log.info("업체 배송 담당자 배송 일일 배정 완료 - 성공={}, 실패={}", successCount, failedHubIds.size());

        if (!failedHubIds.isEmpty()) {
            List<UUID> finalFailed = retryFailedHubs(failedHubIds);
            if (!finalFailed.isEmpty()) {
                log.error("최종 배정 실패 허브 목록 - hubIds={}", finalFailed);
            }
        }
    }

    private List<UUID> retryFailedHubs(List<UUID> failedHubIds) {
        List<UUID> remaining = new ArrayList<>(failedHubIds);

        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("재시도 중 인터럽트 발생 - 남은 실패 허브={}", remaining);
                return remaining;
            }

            List<UUID> stillFailed = new ArrayList<>();
            for (UUID hubId : remaining) {
                try {
                    companyDeliveryAssignmentService.assignDailyCompanyDeliveries(hubId);
                    log.info("재시도 성공 {}/{} - hubId={}", attempt, MAX_RETRY_COUNT, hubId);
                } catch (Exception e) {
                    stillFailed.add(hubId);
                    log.warn("재시도 실패 {}/{} - hubId={}, error={}", attempt, MAX_RETRY_COUNT, hubId, e.getMessage());
                }
            }

            remaining = stillFailed;
            if (remaining.isEmpty()) {
                log.info("재시도 {}회차에 전체 성공", attempt);
                break;
            }
        }

        return remaining;
    }
}