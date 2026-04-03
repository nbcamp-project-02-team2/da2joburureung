package com.da2jobu.infrastructure.client;

import com.da2jobu.infrastructure.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUserInfo(@PathVariable("userId") UUID userId);
}