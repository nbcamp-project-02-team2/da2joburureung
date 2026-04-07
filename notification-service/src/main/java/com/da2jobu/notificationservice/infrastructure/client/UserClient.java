package com.da2jobu.notificationservice.infrastructure.client;

import com.da2jobu.notificationservice.infrastructure.client.dto.UserRole;
import com.da2jobu.notificationservice.infrastructure.client.dto.UserStatus;
import com.da2jobu.notificationservice.infrastructure.client.dto.UserPageResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users")
    CommonResponse<UserPageResponse> getUsers(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,
            @RequestParam("status") UserStatus userStatus,
            @RequestParam("role") UserRole userRole,
            @RequestParam("keyword") String userId
    );
}
