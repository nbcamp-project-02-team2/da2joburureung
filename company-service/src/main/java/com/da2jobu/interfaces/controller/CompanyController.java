package com.da2jobu.interfaces.controller;

import com.da2jobu.application.CompanyService;
import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.command.UpdateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.interfaces.dto.request.CreateCompanyRequest;
import com.da2jobu.interfaces.dto.request.UpdateCompanyRequest;
import com.da2jobu.interfaces.dto.response.CompanyResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Company", description = "업체 관리 API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 업체 생성
     */
    @PostMapping
    public ResponseEntity<CommonResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader(value = "X-User-Hub-Id", required = false) UUID userHubId
    ) {
        CreateCompanyCommand command = new CreateCompanyCommand(
                userRole,
                userHubId,
                request.hubId(),
                request.name(),
                request.type(),
                request.address()
        );
        CompanyResult result = companyService.createCompany(command);
        return CommonResponse.created("업체 생성 완료", CompanyResponse.from(result));
    }

    /**
     * 업체 수정
     */
    @PatchMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyResponse>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader(value = "X-User-Hub-Id", required = false) UUID userHubId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        UpdateCompanyCommand command = new UpdateCompanyCommand(
                companyId,
                userRole,
                userHubId,
                userId,
                request.hubId(),
                request.name(),
                request.type(),
                request.address()
        );
        CompanyResult result = companyService.update(command);
        return CommonResponse.ok("업체 수정 완료", CompanyResponse.from(result));
    }

    /**
     * 업체 단건 조회
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyResponse>> getCompany(
            @PathVariable UUID companyId
    ) {
        CompanyResult result = companyService.getCompany(companyId);
        return CommonResponse.ok("업체 조회 완료", CompanyResponse.from(result));
    }
}