package com.da2jobu.presentation.dto.response;

import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.domain.model.vo.CompanyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyResponse(
        UUID companyId,
        UUID managerId,
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static CompanyResponse from(CompanyResult result) {
        return new CompanyResponse(
                result.companyId(),
                result.managerId(),
                result.hubId(),
                result.name(),
                result.type(),
                result.address(),
                result.latitude(),
                result.longitude()
        );
    }
}