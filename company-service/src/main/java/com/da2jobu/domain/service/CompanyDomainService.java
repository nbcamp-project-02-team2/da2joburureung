package com.da2jobu.domain.service;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyType;
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
            // 본인 소속 허브인지 검증
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
     * - COMPANY_MANAGER: 본인이 담당자인 업체만 수정 가능
     */
    public void validateUpdateAccess(Company company, String role, UUID userHubId, UUID userId) {
        if ("MASTER".equals(role)) return;
        if ("HUB_MANAGER".equals(role)) {
            if (!company.belongsToHub(userHubId)) {
                throw new CustomException(ErrorCode.COMPANY_HUB_MISMATCH);
            }
            return;
        }
        if ("COMPANY_MANAGER".equals(role)) {
            if (!company.hasManager() || !company.getManagerId().getManagerId().equals(userId)) {
                throw new CustomException(ErrorCode.COMPANY_ACCESS_DENIED);
            }
            return;
        }
        throw new CustomException(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
    }

    /**
     * 필드 수정 권한 검증
     * - COMPANY_MANAGER는 허브 ID, 업체 타입, 담당자 ID 수정 불가
     */
    public void validateFieldModificationAccess(String role, Company company, UUID newHubId, CompanyType newType) {
        if ("MASTER".equals(role) || "HUB_MANAGER".equals(role)) return;
        boolean hubChanged = !company.getHubId().getHubId().equals(newHubId);
        boolean typeChanged = !company.getType().equals(newType);
        if (hubChanged || typeChanged) {
            throw new CustomException(ErrorCode.COMPANY_MANAGER_FIELD_FORBIDDEN);
        }
    }
}