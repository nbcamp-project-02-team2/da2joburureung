package com.da2jobu.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderFeignClient {

    /**
     * order에서 내부 api 추가 예정
     * 아직 url은 없음.. 임시로 두기
     */
    @GetMapping("/api/orders/{companyId}")
    boolean hasActiveOrders(@PathVariable("companyId") UUID orderId);
}