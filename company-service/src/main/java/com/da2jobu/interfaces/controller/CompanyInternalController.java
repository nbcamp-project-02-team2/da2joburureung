package com.da2jobu.interfaces.controller;

import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.service.CompanyService;
import com.da2jobu.interfaces.dto.response.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal/companies")
@RequiredArgsConstructor
public class CompanyInternalController {

    private final CompanyService companyService;

    @PostMapping
    public List<CompanyResponse> getCompanies(
            @RequestBody List<UUID> companyIds
    ) {
        return companyService.getCompaniesByIds(companyIds)
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    @GetMapping("/{companyId}")
    public CompanyResponse getCompanyAddress(
            @PathVariable UUID companyId
    ) {
        log.debug("내부 API 업체 주소 조회: companyId={}", companyId);
        CompanyResult result = companyService.getCompany(companyId);
        return CompanyResponse.from(result);
    }
}