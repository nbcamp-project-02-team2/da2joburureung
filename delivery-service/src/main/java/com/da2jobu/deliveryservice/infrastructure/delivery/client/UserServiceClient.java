package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.UserInfoDto;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/by-username")
    UserInfoDto getUserByUsername(@RequestParam("username") String username);

    @GetMapping("/api/internal/users/by-userId/{userId}")
    com.da2jobu.deliveryservice.infrastructure.dto.UserInfoByIdDto getUserByUserId(@PathVariable UUID userId);
}
