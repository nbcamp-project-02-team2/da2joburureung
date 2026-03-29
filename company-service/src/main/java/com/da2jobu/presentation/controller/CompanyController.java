package com.da2jobu.presentation.controller;

import com.da2jobu.application.CompanyService;
import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.presentation.dto.request.CreateCompanyRequest;
import com.da2jobu.presentation.dto.response.CompanyResponse;
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
        validateCreatePermission(userRole, userHubId, request.hubId());
        CreateCompanyCommand createCompanyCommand = new CreateCompanyCommand(
                request.hubId(),
                request.name(),
                request.type(),
                request.address()
        );

        CompanyResult result = companyService.create(createCompanyCommand);
        CompanyResponse response = CompanyResponse.from(result);
        return CommonResponse.created("업체 생성 완료", response);
    }

    private void validateCreatePermission(String userRole, UUID userHubId, UUID requestedHubId) {
        if ("MASTER".equals(userRole)) {
            return;
        }
        if ("HUB_MANAGER".equals(userRole)) {
            if (userHubId == null || !userHubId.equals(requestedHubId)) {
                throw new IllegalArgumentException("담당 허브에만 업체를 생성할 수 있습니다.");
            }
            return;
        }
        throw new SecurityException("업체 생성 권한이 없습니다. role: " + userRole);
    }
}