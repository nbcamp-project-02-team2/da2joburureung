package com.da2jobu.productservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * 업체 서비스 FeignClient.
 * - 상품 생성 시 업체 ID 존재 여부 검증에 사용
 * - 허브 관리자 권한 검증 시 업체의 소속 허브 ID 조회에 사용
 */
@FeignClient(name = "company-service", fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {

    /** 업체 ID 존재 여부 확인 */
    @GetMapping("/api/companies/{companyId}/exists")
    Boolean existsCompany(@PathVariable("companyId") UUID companyId);

    /** 업체가 소속된 허브 ID 조회 */
    @GetMapping("/api/companies/{companyId}/hub-id")
    UUID getHubIdByCompanyId(@PathVariable("companyId") UUID companyId);
}
