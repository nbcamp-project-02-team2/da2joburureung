package com.da2jobu.presentation.dto.response;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(
        UUID companyId,
        UUID managerId,
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt,
        String createdBy
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getManagerId(),
                company.getHubId(),
                company.getName(),
                company.getType(),
                company.getLocation().getAddress(),
                company.getLocation().getLatitude(),
                company.getLocation().getLongitude(),
                company.getCreatedAt(),
                company.getCreatedBy()
        );
    }
}