package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubPathResponseDto;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-path-service")
public interface HubPathServiceClient {

    @GetMapping("/api/internal/hub-paths/{hubPathId}")
    CommonResponse<HubPathResponseDto> getHubPath(
            @PathVariable("hubPathId") String hubPathId,
            @RequestParam(value = "departHubName", required = false) String departHubName,
            @RequestParam(value = "arriveHubName", required = false) String arriveHubName
    );
}
