package com.da2jobu.application.dto.result;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyResult(
        UUID companyId,
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static CompanyResult from(Company company) {
        return new CompanyResult(
                company.getCompanyId().getCompanyId(),
                company.getHubId().getHubId(),
                company.getName(),
                company.getType(),
                company.getLocation().getAddress(),
                company.getLocation().getLatitude(),
                company.getLocation().getLongitude()
        );
    }
}
