package com.da2jobu.application;

import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.command.SearchCompanyCommand;
import com.da2jobu.application.dto.command.UpdateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.service.CompanyEventPublisher;
import com.da2jobu.application.service.HubClient;
import com.da2jobu.application.service.LocationClient;
import com.da2jobu.application.service.OrderClient;
import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyId;
import com.da2jobu.domain.model.vo.HubId;
import com.da2jobu.domain.model.vo.Location;
import com.da2jobu.domain.repository.CompanyRepository;
import com.da2jobu.domain.service.CompanyDomainService;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    // ========== Application Services ==========
    private final LocationClient locationClient;
    private final HubClient hubClient;
    private final OrderClient orderClient;
    private final CompanyEventPublisher companyEventPublisher;
    // ========== Domain ==========
    private final CompanyRepository companyRepository;
    private final CompanyDomainService companyDomainService;


    @Transactional
    public CompanyResult createCompany(CreateCompanyCommand command) {
        log.info("업체 생성 요청: name={}, hubId={}, role={}", command.name(), command.hubId(), command.userRole());
        //업체 생성 권한 검증
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
        log.info("업체 생성 완료: companyId={}", savedCompany.getCompanyId().getCompanyId());
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
        log.info("업체 수정 요청: companyId={}, role={}", command.companyId(), command.userRole());
        Company company = findCompanyOrThrow(command.companyId());
        //접근 권한 및 필드 수정 권한 검증
        companyDomainService.validateUpdateAccess(company, command.userRole(), command.userHubId(), command.userCompanyId());
        //변경된 허브 id 존재 여부 검증
        if (company.isHubChanged(command.hubId())) {
            validateHubExists(command.hubId());
        }
        //주소 변동 시 카카오 api 새로 호출 후 추출
        Location location = company.isAddressChanged(command.address())
                ? locationClient.resolveLocation(command.address())
                : company.getLocation();

        company.update(
                command.name(),
                command.type(),
                HubId.of(command.hubId()),
                location
        );
        log.info("업체 수정 완료: companyId={}", command.companyId());
        return CompanyResult.from(companyRepository.save(company));
    }

    @Transactional
    @CacheEvict(value = "company", key = "#companyId")
    public void deleteCompany(UUID companyId, String userRole, UUID userHubId, String deletedBy) {
        log.info("업체 삭제 요청: companyId={}, role={}, deletedBy={}", companyId, userRole, deletedBy);
        Company company = findCompanyOrThrow(companyId);
        companyDomainService.validateDeleteAccess(company, userRole, userHubId);
        //배송중일땐 삭제 불가
        if (orderClient.hasActiveOrders(companyId)) {
            log.warn("업체 삭제 거부, 진행 중인 주문 존재: companyId={}", companyId);
            throw new CustomException(ErrorCode.COMPANY_HAS_ACTIVE_ORDERS);
        }
        company.softDelete(deletedBy);
        companyEventPublisher.publishCompanyDeleted(companyId, deletedBy, company.getDeletedAt());
        log.info("업체 삭제 완료: companyId={}", companyId);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResult> searchCompanies(SearchCompanyCommand command) {
        log.debug("업체 검색 요청: name={}, type={}, hubId={}, page={}, size={}", command.name(), command.type(), command.hubId(), command.validatedPage(), command.validatedSize());
        PageRequest pageable = PageRequest.of(command.validatedPage(), command.validatedSize());
        return companyRepository.search(command.name(), command.type(), command.hubId(), pageable)
                .map(CompanyResult::from);
    }


    private Company findCompanyOrThrow(UUID companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));
    }

    private void validateHubExists(UUID hubId) {
        if (!hubClient.validateHubExists(hubId)) {
            throw new CustomException(ErrorCode.HUB_NOT_FOUND);
        }
    }

}