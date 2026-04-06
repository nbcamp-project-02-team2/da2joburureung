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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "업체 생성", description = "MASTER 또는 HUB_MANAGER 권한으로 업체를 생성합니다.")
    @PostMapping
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
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
    @Operation(summary = "업체 수정", description = "MASTER, HUB_MANAGER, COMPANY_MANAGER 권한으로 업체를 수정합니다.")
    @PutMapping("/{companyId}")
    @RequireRoles({"MASTER", "HUB_MANAGER", "COMPANY_MANAGER"})
    public ResponseEntity<CommonResponse<CompanyResponse>> updateCompany(
            @Parameter(description = "수정할 업체 ID", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            @Parameter(description = "요청 사용자 역할", example = "COMPANY_MANAGER")
            @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
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
    @Operation(summary = "업체 목록 조회", description = "업체명, 타입, 허브 ID 조건으로 업체 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<CompanyResponse>>> searchCompanies(
            @Parameter(description = "업체명 검색어", example = "서울")
            @RequestParam(required = false) String name,
            @Parameter(description = "업체 타입", example = "PRODUCER")
            @RequestParam(required = false) CompanyType type,
            @Parameter(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111")
            @RequestParam(required = false) UUID hubId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        SearchCompanyCommand command = new SearchCompanyCommand(name, type, hubId, page, size);
        Page<CompanyResponse> result = companyService.searchCompanies(command).map(CompanyResponse::from);
        return CommonResponse.ok("업체 목록 조회 완료", result);
    }

    /**
     * 업체 단건 조회
     */
    @Operation(summary = "업체 단건 조회", description = "업체 ID로 업체 상세 정보를 조회합니다.")
    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyResponse>> getCompany(
            @Parameter(description = "조회할 업체 ID", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID companyId
    ) {
        CompanyResult result = companyService.getCompany(companyId);
        return CommonResponse.ok("업체 조회 완료", CompanyResponse.from(result));
    }

    /**
     * 업체 삭제
     */
    @Operation(summary = "업체 삭제", description = "MASTER 또는 HUB_MANAGER 권한으로 업체를 삭제합니다.")
    @DeleteMapping("/{companyId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<?>> deleteCompany(
            @Parameter(description = "삭제할 업체 ID", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID companyId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID userId
    ) {
        companyService.deleteCompany(companyId, userRole, userId);
        return CommonResponse.noContent();
    }
}