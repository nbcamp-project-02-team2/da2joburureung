package com.delivery.hubpath.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "HUB-SERVICE")
public interface HubClient {

    @GetMapping("/api/hubs")
    CommonResponse<PageResponse<HubResponse>> getHubs(
            @RequestParam(value = "hub_name", required = false) String hubName,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "page") int page
    );

    @GetMapping("/api/hubs/all")
    CommonResponse<List<HubResponse>> getAllHubs();
}