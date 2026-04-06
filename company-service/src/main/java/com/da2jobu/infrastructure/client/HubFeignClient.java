package com.da2jobu.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubFeignClient {

    @GetMapping("/api/internal/hubs")
    CommonResponse getHubs(@RequestParam(value = "hub_id", required = false) UUID hubId,
                           @RequestParam(value = "size") int size,
                           @RequestParam(value = "page") int page);
}
