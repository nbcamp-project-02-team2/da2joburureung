package com.da2jobu.deliveryservice.infrastructure.client;

import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/by-username")
    UserInfoDto getUserByUsername(@RequestParam("username") String username);

    /**
     * todo : 유저에서 추가 예정
     */
    @GetMapping("/api/internal/users/by-userId")
    UserInfoDto getUserByUserId(@RequestParam("userId") UUID userId);
}
