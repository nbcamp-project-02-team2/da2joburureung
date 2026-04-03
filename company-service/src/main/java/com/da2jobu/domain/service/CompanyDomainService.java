package com.da2jobu.domain.service;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.HubId;
import common.exception.CustomException;
import common.exception.ErrorCode;

import java.util.UUID;

public class CompanyDomainService {

    /**
     * 업체 생성 접근 권한 검증
     * - MASTER: 모든 허브에 생성 가능
     * - HUB_MANAGER: 담당 허브에만 생성 가능
     */
    public void validateCreateAccess(String role, UUID userHubId, UUID requestedHubId) {
        if ("MASTER".equals(role)) return;
        if ("HUB_MANAGER".equals(role)) {
            if (userHubId == null || !userHubId.equals(requestedHubId)) {
                throw new CustomException(ErrorCode.COMPANY_HUB_MISMATCH);
            }
            return;
        }
        throw new CustomException(ErrorCode.COMPANY_CREATE_FORBIDDEN);
    }

    /**
     * 업체 수정 접근 권한 검증
     * - MASTER: 모든 업체 수정 가능
     * - HUB_MANAGER: 담당 허브 소속 업체만 수정 가능
     */
    public void validateUpdateAccess(Company company, String role, UUID userHubId, UUID userCompanyId) {
        if ("MASTER".equals(role)) return;
        if ("HUB_MANAGER".equals(role)) {
            validateHubAccess(company, userHubId);
            return;
        }
        if ("COMPANY_MANAGER".equals(role)) {
            if (userCompanyId == null || !company.getCompanyId().isSameAs(userCompanyId)) {
                throw new CustomException(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
            }
            return;
        }
        throw new CustomException(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
    }

    /**
     * 업체 삭제 접근 권한 검증
     * - MASTER: 모든 업체 삭제 가능
     * - HUB_MANAGER: 담당 허브 소속 업체만 삭제 가능
     */
    public void validateDeleteAccess(Company company, String role, UUID userHubId) {
        if ("MASTER".equals(role)) return;
        if ("HUB_MANAGER".equals(role)) {
            validateHubAccess(company, userHubId);
            return;
        }
        throw new CustomException(ErrorCode.COMPANY_DELETE_FORBIDDEN);
    }

    /**
     * 업체 소속 권한 검증
     * - HUB_MANAGER: 담당 허브 소속 업체 여부
     */
    private void validateHubAccess(Company company, UUID userHubId) {
        if (!company.belongsToHub(HubId.of(userHubId))) {
            throw new CustomException(ErrorCode.COMPANY_HUB_MISMATCH);
        }
    }

}