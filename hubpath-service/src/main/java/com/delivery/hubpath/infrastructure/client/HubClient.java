package com.delivery.hubpath.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;


@FeignClient(name = "hub-service", url = "http://localhost:8084")
public interface HubClient {

    @GetMapping("/api/hubs/search-name")
    CommonResponse<HubResponse> getHubByName(@RequestParam("hubname") String hubname);

    @GetMapping("/api/hubs")
    CommonResponse<Page<HubResponse>> getAllHubs();
}