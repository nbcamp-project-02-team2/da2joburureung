package com.da2jobu.interfaces.controller;

import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.service.CompanyService;
import com.da2jobu.interfaces.dto.response.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Internal Company", description = "내부 서비스 간 업체 조회 API")
@RestController
@RequestMapping("/api/internal/companies")
@RequiredArgsConstructor
public class CompanyInternalController {

    private final CompanyService companyService;

    @Operation(summary = "업체 다건 내부 조회", description = "다른 서비스에서 업체 ID 목록으로 여러 업체를 조회합니다.")
    @PostMapping
    public List<CompanyResponse> getCompanies(
            @RequestBody List<UUID> companyIds
    ) {
        return companyService.getCompaniesByIds(companyIds)
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    @Operation(summary = "업체 단건 내부 조회", description = "다른 서비스에서 업체 ID로 단건 조회합니다.")
    @GetMapping("/{companyId}")
    public CompanyResponse getCompanyAddress(
            @Parameter(description = "조회할 업체 ID", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID companyId
    ) {
        log.debug("내부 API 업체 주소 조회: companyId={}", companyId);
        CompanyResult result = companyService.getCompany(companyId);
        return CompanyResponse.from(result);
    }
}