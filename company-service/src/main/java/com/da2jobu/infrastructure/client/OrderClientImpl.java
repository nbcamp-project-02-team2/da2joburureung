package com.da2jobu.infrastructure.client;

import com.da2jobu.application.client.OrderClient;
import common.exception.CustomException;
import common.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClientImpl implements OrderClient {

    private final OrderFeignClient orderFeignClient;

    @Override
    @Retry(name = "orderService")
    @CircuitBreaker(name = "orderService", fallbackMethod = "orderServiceFallback")
    public boolean hasActiveOrders(UUID companyId) {
        // FeignException은 Retry가 재시도, 소진 시 CB fallback
        return orderFeignClient.countActiveOrders(companyId).getData() > 0;
    }

    private boolean orderServiceFallback(UUID companyId, Throwable t) {
        log.error("CircuitBreaker 주문 서비스 호출 불가: companyId={}, cause={}", companyId, t.getMessage());
        // 주문 서비스 장애 시 삭제를 허용하지 않음 (안전한 방향)
        throw new CustomException(ErrorCode.ORDER_SERVICE_ERROR);
    }
}
