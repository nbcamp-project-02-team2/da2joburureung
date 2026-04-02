package com.da2jobu.application.dto.command;

import com.da2jobu.domain.model.vo.CompanyType;

import java.util.UUID;

public record CreateCompanyCommand(
        String userRole,
        UUID userHubId,
        UUID hubId,
        String name,
        CompanyType type,
        String address
) {
}
