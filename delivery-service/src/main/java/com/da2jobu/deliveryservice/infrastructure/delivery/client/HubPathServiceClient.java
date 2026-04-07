package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubPathResponseDto;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "hubpath-service")
public interface HubPathServiceClient {

    @GetMapping("/api/internal/hub-paths/search")
    CommonResponse<HubPathResponseDto> getHubPath(
            @RequestParam("departHubName") String departHubName,
            @RequestParam("arriveHubName") String arriveHubName
    );
}
