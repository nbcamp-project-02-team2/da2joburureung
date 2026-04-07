package com.da2jobu.domain.service;

import com.da2jobu.domain.model.entity.Company;
import com.da2jobu.domain.model.vo.CompanyId;
import com.da2jobu.domain.model.vo.CompanyType;
import com.da2jobu.domain.model.vo.HubId;
import com.da2jobu.domain.model.vo.Location;
import common.exception.CustomException;
import common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyDomainServiceTest {

    private CompanyDomainService domainService;
    private Company company;

    private final UUID companyUUID = UUID.randomUUID();
    private final UUID hubUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        domainService = new CompanyDomainService();
        company = Company.create(
                CompanyId.of(companyUUID),
                HubId.of(hubUUID),
                "테스트업체",
                CompanyType.PRODUCER,
                Location.of("서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0))
        );
    }

    @Nested
    @DisplayName("validateHubAccess")
    class ValidateHubAccess {

        @Test
        @DisplayName("담당 허브 일치 - 통과")
        void success() {
            assertThatCode(() -> domainService.validateHubAccess(company, hubUUID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userHubId null - COMPANY_HUB_MISMATCH")
        void nullHubId() {
            assertThatThrownBy(() -> domainService.validateHubAccess(company, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_HUB_MISMATCH);
        }

        @Test
        @DisplayName("담당 허브 불일치 - COMPANY_HUB_MISMATCH")
        void hubMismatch() {
            assertThatThrownBy(() -> domainService.validateHubAccess(company, UUID.randomUUID()))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_HUB_MISMATCH);
        }
    }

    @Nested
    @DisplayName("validateCompanyAccess")
    class ValidateCompanyAccess {

        @Test
        @DisplayName("본인 업체 - 통과")
        void success() {
            assertThatCode(() -> domainService.validateCompanyAccess(company, companyUUID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userCompanyId null - COMPANY_UPDATE_FORBIDDEN")
        void nullCompanyId() {
            assertThatThrownBy(() -> domainService.validateCompanyAccess(company, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
        }

        @Test
        @DisplayName("다른 업체 - COMPANY_UPDATE_FORBIDDEN")
        void companyMismatch() {
            assertThatThrownBy(() -> domainService.validateCompanyAccess(company, UUID.randomUUID()))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_UPDATE_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("validateHubCreateAccess")
    class ValidateHubCreateAccess {

        @Test
        @DisplayName("허브 일치 - 통과")
        void success() {
            assertThatCode(() -> domainService.validateHubCreateAccess(hubUUID, hubUUID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("userHubId null - COMPANY_HUB_MISMATCH")
        void nullUserHubId() {
            assertThatThrownBy(() -> domainService.validateHubCreateAccess(hubUUID, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_HUB_MISMATCH);
        }

        @Test
        @DisplayName("허브 불일치 - COMPANY_HUB_MISMATCH")
        void hubMismatch() {
            assertThatThrownBy(() -> domainService.validateHubCreateAccess(hubUUID, UUID.randomUUID()))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_HUB_MISMATCH);
        }
    }
}