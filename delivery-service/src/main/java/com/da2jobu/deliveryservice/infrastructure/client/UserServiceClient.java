package com.da2jobu.deliveryservice.infrastructure.client;

import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/by-username")
    UserInfoDto getUserByUsername(@RequestParam("username") String username);
}
