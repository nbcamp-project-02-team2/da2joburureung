package com.da2jobu.deliveryservice.infrastructure.delivery.client;

<<<<<<<< HEAD:delivery-service/src/main/java/com/da2jobu/deliveryservice/infrastructure/delivery/client/HubFeignClient.java
import com.da2jobu.deliveryservice.infrastructure.delivery.dto.HubResponse;
========
import com.da2jobu.deliveryservice.infrastructure.dto.HubListResponse;
import com.da2jobu.deliveryservice.infrastructure.dto.HubResponse;
import com.da2jobu.deliveryservice.infrastructure.dto.PageResponse;
>>>>>>>> 494036464ad67a239263f7e92f80d88e46b0537e:delivery-service/src/main/java/com/da2jobu/deliveryservice/infrastructure/delivery/client/HubServiceClient.java
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "hub-service")
public interface HubServiceClient {

    @GetMapping("/api/hubs/{hubId}")
    CommonResponse<HubResponse> getHub(@PathVariable UUID hubId);

    @GetMapping("/api/hubs")
    CommonResponse<PageResponse<HubListResponse>> getHubs(@RequestParam int size);
}