package com.da2jobu.productservice.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 사용자 서비스 FeignClient.
 * - 권한 검증 시 사용자의 소속 허브 ID / 업체 ID 조회에 사용
 */
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    /** 사용자의 소속 허브 ID, 업체 ID 조회 */
    @GetMapping("/api/users/me")
    CommonResponse<UserInfoResponse> getMyInfo(
            @RequestHeader("X-User-Id") String userId
    );
}
