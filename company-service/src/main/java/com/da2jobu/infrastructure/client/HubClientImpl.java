package com.da2jobu.infrastructure.client;

import com.da2jobu.application.client.HubClient;
import common.exception.CustomException;
import common.exception.ErrorCode;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubClientImpl implements HubClient {

    private final HubFeignClient hubFeignClient;

    @Override
    @Retry(name = "hubService")
    @CircuitBreaker(name = "hubService", fallbackMethod = "hubServiceFallback")
    public boolean validateHubExists(UUID hubId) {
        try {
            hubFeignClient.getHub(hubId);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
        // 그 외 FeignException은 Retry가 재시도, 소진 시 CB fallback
    }

    private void hubServiceFallback(UUID hubId, Throwable t) {
        log.error("CircuitBreaker 허브 서비스 호출 불가: hubId={}, cause={}", hubId, t.getMessage());
        throw new CustomException(ErrorCode.HUB_SERVICE_ERROR);
    }
}
