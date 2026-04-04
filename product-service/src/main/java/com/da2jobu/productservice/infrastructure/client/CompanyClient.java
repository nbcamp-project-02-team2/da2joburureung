package com.da2jobu.productservice.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * 업체 서비스 FeignClient.
 * - 상품 생성 시 업체 존재 여부 및 소속 허브 검증에 사용
 */
@FeignClient(name = "company-service", fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {

    /** 업체 단건 조회 (존재 여부 + 소속 허브 ID 동시 확인) */
    @GetMapping("/api/companies/{companyId}")
    CommonResponse<CompanyInfoResponse> getCompany(@PathVariable("companyId") UUID companyId);
}
