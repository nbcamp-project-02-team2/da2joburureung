package com.da2jobu.orderservice.infrastructure.client;

import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class CompanyClientFallbackFactory implements FallbackFactory<CompanyClient> {

    @Override
    public CompanyClient create(Throwable cause) {
        log.error("CompanyClient fallback 실행. 원인: {}", cause.getMessage());
        return new CompanyClient() {
            @Override
            public CommonResponse<CompanyInfoResponse> getCompany(UUID companyId) {
                throw new CustomException(ErrorCode.COMPANY_SERVICE_UNAVAILABLE);
            }
        };
    }
}
