package com.da2jobu.deliverymanagerservice.infrastructure.client;

import com.da2jobu.deliverymanagerservice.infrastructure.client.dto.HubResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubFeignClient {

    @GetMapping("/api/hubs/{hubId}")
    CommonResponse<HubResponse> getHub(@PathVariable UUID hubId);
}