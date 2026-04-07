package com.da2jobu.orderservice.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company-service", fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {

    @GetMapping("/api/companies/{companyId}")
    CommonResponse<CompanyInfoResponse> getCompany(@PathVariable("companyId") UUID companyId);
}
