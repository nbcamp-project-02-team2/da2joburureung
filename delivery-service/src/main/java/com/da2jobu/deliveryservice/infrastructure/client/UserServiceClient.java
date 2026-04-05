package com.da2jobu.deliveryservice.infrastructure.client;

import com.da2jobu.deliveryservice.infrastructure.dto.UserInfoDto;
import com.da2jobu.deliveryservice.infrastructure.dto.UserResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/by-username")
    UserInfoDto getUserByUsername(@RequestParam("username") String username);

    /**
     * todo : 유저에서 추가 예정
     */
    @GetMapping("/api/users/{userId}")
    CommonResponse<UserResponse> getUser(
            @PathVariable("userId") UUID userId,
            @RequestHeader("X-User-Role") String role
    );
}
