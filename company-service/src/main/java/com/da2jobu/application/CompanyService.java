package com.da2jobu.application;

import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.command.UpdateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.service.HubClient;
import com.da2jobu.application.service.LocationClient;
import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyId;
import com.da2jobu.domain.model.vo.HubId;
import com.da2jobu.domain.model.vo.Location;
import com.da2jobu.domain.repository.CompanyRepository;
import com.da2jobu.domain.service.CompanyDomainService;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {
    // ========== Application Services ==========
    private final LocationClient locationClient;
    private final HubClient hubClient;
    // ========== Domain ==========
    private final CompanyRepository companyRepository;
    private final CompanyDomainService companyDomainService;


    @Transactional
    public CompanyResult createCompany(CreateCompanyCommand command) {
        //업체생성 권한 검증
        companyDomainService.validateCreateAccess(command.userRole(), command.userHubId(), command.hubId());
        //허브 존재 여부 검증
        validateHubExists(command.hubId());

        Location location = locationClient.resolveLocation(command.address());
        Company company = Company.create(
                CompanyId.of(),
                HubId.of(command.hubId()),
                command.name(),
                command.type(),
                location
        );
        Company savedCompany = companyRepository.save(company);
        return CompanyResult.from(savedCompany);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "company", key = "#companyId")
    public CompanyResult getCompany(UUID companyId) {
        return CompanyResult.from(findCompanyOrThrow(companyId));
    }

    @Transactional
    @CachePut(value = "company", key = "#command.companyId")
    public CompanyResult update(UpdateCompanyCommand command) {
        Company company = findCompanyOrThrow(command.companyId());
        //접근 권한 및 필드 수정 권한 검증
        companyDomainService.validateUpdateAccess(company, command.userRole(), command.userHubId(), command.userId());
        companyDomainService.validateFieldModificationAccess(command.userRole(), company, command.hubId(), command.type());
        //바뀐 허브 id 검증
        if (!company.getHubId().getHubId().equals(command.hubId())) {
            hubClient.validateHubExists(command.hubId());
        }
        //주소 변동 시 카카오 api 새로 호출 후 추출
        Location location = command.address().equals(company.getLocation().getAddress())
                ? company.getLocation()
                : locationClient.resolveLocation(command.address());

        company.update(
                command.name(),
                command.type(),
                HubId.of(command.hubId()),
                location
        );
        return CompanyResult.from(company);
    }

    @Transactional
    @CachePut(value = "company", key = "#companyId")
    public void assignManager(UUID companyId, UUID managerId) {
        Company company = findCompanyOrThrow(companyId);
        company.updateManagerId(managerId);
    }

    private Company findCompanyOrThrow(UUID companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));
    }

    /**
     * 도메인 외부 검증 로직
     */
    private void validateHubExists(UUID hubId) {
        hubClient.validateHubExists(hubId);
    }

}