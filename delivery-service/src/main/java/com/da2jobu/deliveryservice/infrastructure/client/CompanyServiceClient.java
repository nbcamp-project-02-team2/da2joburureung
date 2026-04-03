package com.da2jobu.deliveryservice.infrastructure.client;

import com.da2jobu.deliveryservice.infrastructure.dto.CompanyInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyServiceClient {

    @GetMapping("/api/internal/companies/{companyId}")
    CompanyInfoDto getCompany(@PathVariable("companyId") UUID companyId);
}
