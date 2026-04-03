package com.da2jobu.interfaces.controller;

import com.da2jobu.application.CompanyService;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.interfaces.dto.response.CompanyResponse;
import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/internal/companies")
@RequiredArgsConstructor
public class CompanyInternalController {

    private final CompanyService companyService;

    @Value("${internal.token}")
    private String internalToken;

    /**
     * 업체 주소 조회 (내부 API)
     * - 배송 서비스에서 FeignClient로 호출
     * - X-Internal-Token 헤더로 서비스 간 인증
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<CommonResponse<CompanyResponse>> getCompanyAddress(
            @PathVariable UUID companyId,
            @RequestHeader("X-Internal-Token") String token
    ) {
        if (!internalToken.equals(token)) {
            log.warn("내부 API 인증 실패: companyId={}", companyId);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        log.debug("내부 API 업체 주소 조회: companyId={}", companyId);
        CompanyResult result = companyService.getCompany(companyId);
        return CommonResponse.ok("업체 주소 조회 완료", CompanyResponse.from(result));
    }
}