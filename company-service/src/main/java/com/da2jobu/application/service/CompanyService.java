package com.da2jobu.application.service;

import com.da2jobu.application.client.HubClient;
import com.da2jobu.application.client.LocationClient;
import com.da2jobu.application.client.OrderClient;
import com.da2jobu.application.client.UserClient;
import com.da2jobu.application.dto.command.CreateCompanyCommand;
import com.da2jobu.application.dto.command.SearchCompanyCommand;
import com.da2jobu.application.dto.command.UpdateCompanyCommand;
import com.da2jobu.application.dto.result.CompanyResult;
import com.da2jobu.application.messaging.CompanyEventPublisher;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {
    // ========== Application Services ==========
    private final LocationClient locationClient;
    private final HubClient hubClient;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final CompanyEventPublisher companyEventPublisher;
    // ========== Domain ==========
    private final CompanyRepository companyRepository;
    private final CompanyDomainService companyDomainService;


    @Transactional
    public CompanyResult createCompany(CreateCompanyCommand command) {
        log.info("업체 생성 요청: name={}, hubId={}, role={}", command.name(), command.hubId(), command.userRole());
        UserClient.UserInfo userInfo = validateUserExists(command.userId());
        if ("HUB_MANAGER".equals(command.userRole())) {
            companyDomainService.validateHubCreateAccess(command.hubId(), userInfo.hubId());
        }
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
        UserClient.UserInfo userInfo = validateUserExists(command.userId());
        if ("HUB_MANAGER".equals(command.userRole())) {
            companyDomainService.validateHubAccess(company, userInfo.hubId());
        } else if ("COMPANY_MANAGER".equals(command.userRole())) {
            companyDomainService.validateCompanyAccess(company, userInfo.companyId());
        }
        if (company.isHubChanged(command.hubId())) {
            validateHubExists(command.hubId());
        }
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
    public void deleteCompany(UUID companyId, String userRole, UUID userId) {
        log.info("업체 삭제 요청: companyId={}, role={}, deletedBy={}", companyId, userRole, userId);
        Company company = findCompanyOrThrow(companyId);
        UserClient.UserInfo userInfo = validateUserExists(userId);
        if ("HUB_MANAGER".equals(userRole)) {
            companyDomainService.validateHubAccess(company, userInfo.hubId());
        }
        if (orderClient.hasActiveOrders(companyId)) {
            log.warn("업체 삭제 거부, 진행 중인 주문 존재: companyId={}", companyId);
            throw new CustomException(ErrorCode.COMPANY_HAS_ACTIVE_ORDERS);
        }
        company.softDelete(userId.toString());
        companyEventPublisher.publishCompanyDeleted(companyId, userId.toString(), company.getDeletedAt());
        log.info("업체 삭제 완료: companyId={}", companyId);
    }

    @Transactional(readOnly = true)
    public Page<CompanyResult> searchCompanies(SearchCompanyCommand command) {
        log.debug("업체 검색 요청: name={}, type={}, hubId={}, page={}, size={}", command.name(), command.type(), command.hubId(), command.validatedPage(), command.validatedSize());
        PageRequest pageable = PageRequest.of(command.validatedPage(), command.validatedSize());
        return companyRepository.search(command.name(), command.type(), command.hubId(), pageable)
                .map(CompanyResult::from);
    }

    @Transactional(readOnly = true)
    public List<CompanyResult> getCompaniesByIds(List<UUID> companyIds) {
        return companyRepository.findAllByIdsAndDeletedAtIsNull(companyIds)
                .stream()
                .map(CompanyResult::from)
                .toList();
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

    private UserClient.UserInfo validateUserExists(UUID userId) {
        UserClient.UserInfo userInfo = userClient.getUserInfo(userId);
        if (userInfo == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return userInfo;
    }

}