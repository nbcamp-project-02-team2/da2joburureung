package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubResponse;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubListResponse;
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.PageResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "HUB-SERVICE")
public interface HubServiceClient {

    @GetMapping("/api/hubs/{hubId}")
    CommonResponse<HubResponse> getHub(@PathVariable UUID hubId);

    @GetMapping("/api/hubs")
    CommonResponse<PageResponse<HubListResponse>> getHubs(@RequestParam int size);
}