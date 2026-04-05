package com.da2jobu.domain.service;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.HubId;
import common.exception.CustomException;
import common.exception.ErrorCode;

import java.util.UUID;

public class CompanyDomainService {

    /**
     * 허브 관리자용
     * - 담당 허브 업체만 접근 가능
     */
    public void validateHubAccess(Company company, UUID userHubId) {
        if (userHubId == null || !company.belongsToHub(HubId.of(userHubId))) {
            throw new CustomException(ErrorCode.COMPANY_HUB_MISMATCH);
        }
    }

    /**
     * 업체 담당자용
     * - 본인 업체만 접근 가능
     */
    public void validateCompanyAccess(Company company, UUID userCompanyId) {
        if (userCompanyId == null || !company.getCompanyId().isSameAs(userCompanyId)) {
            throw new CustomException(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
        }
    }

    /**
     * 허브 관리자용
     * - 담당 허브 업체만 등록 가능
     */
    public void validateHubCreateAccess(UUID requestHubId, UUID userHubId) {
        if (userHubId == null || !userHubId.equals(requestHubId)) {
            throw new CustomException(ErrorCode.COMPANY_HUB_MISMATCH);
        }
    }
}