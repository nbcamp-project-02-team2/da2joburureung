package com.da2jobu.interfaces.controller;

import com.da2jobu.application.service.CompanyService;
import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.command.SearchCompanyCommand;
import com.da2jobu.application.dto.command.UpdateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.domain.model.vo.CompanyType;
import com.da2jobu.interfaces.dto.request.CreateCompanyRequest;
import com.da2jobu.interfaces.dto.request.UpdateCompanyRequest;
import com.da2jobu.interfaces.dto.response.CompanyResponse;
import com.da2jobu.interfaces.annotation.RequireRoles;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        CreateCompanyCommand command = new CreateCompanyCommand(
                userRole,
                userId,
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
    @PutMapping("/{companyId}")
    @RequireRoles({"MASTER", "HUB_MANAGER", "COMPANY_MANAGER"})
    public ResponseEntity<CommonResponse<CompanyResponse>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        UpdateCompanyCommand command = new UpdateCompanyCommand(
                companyId,
                userRole,
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
     * 업체 목록 검색
     */
    @GetMapping
    public ResponseEntity<CommonResponse<Page<CompanyResponse>>> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SearchCompanyCommand command = new SearchCompanyCommand(name, type, hubId, page, size);
        Page<CompanyResponse> result = companyService.searchCompanies(command).map(CompanyResponse::from);
        return CommonResponse.ok("업체 목록 조회 완료", result);
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

    /**
     * 업체 삭제
     */
    @DeleteMapping("/{companyId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<?>> deleteCompany(
            @PathVariable UUID companyId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        companyService.deleteCompany(companyId, userRole, userId);
        return CommonResponse.noContent();
    }
}