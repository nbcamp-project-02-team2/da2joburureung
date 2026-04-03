package com.da2jobu.deliverymanagerservice.infrastructure.client;

import com.da2jobu.deliverymanagerservice.infrastructure.client.dto.UserResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/users/{userId}")
    CommonResponse<UserResponse> getUser(@PathVariable("userId") UUID userId);
}