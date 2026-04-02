package com.da2jobu.productservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * 사용자 서비스 FeignClient.
 * - 권한 검증 시 사용자의 소속 허브 ID / 업체 ID 조회에 사용
 * - p_user 테이블에 hub_id, company_id가 존재
 */
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    /** 사용자의 소속 허브 ID 조회 */
    @GetMapping("/api/users/{userId}/hub-id")
    UUID getHubIdByUserId(@PathVariable("userId") UUID userId);

    /** 사용자의 소속 업체 ID 조회 */
    @GetMapping("/api/users/{userId}/company-id")
    UUID getCompanyIdByUserId(@PathVariable("userId") UUID userId);
}
