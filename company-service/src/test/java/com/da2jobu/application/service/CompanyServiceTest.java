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
import com.da2jobu.domain.model.vo.CompanyType;
import com.da2jobu.domain.model.vo.HubId;
import com.da2jobu.domain.model.vo.Location;
import com.da2jobu.domain.repository.CompanyRepository;
import com.da2jobu.domain.service.CompanyDomainService;
import common.exception.CustomException;
import common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @InjectMocks
    private CompanyService companyService;

    @Mock private LocationClient locationClient;
    @Mock private HubClient hubClient;
    @Mock private OrderClient orderClient;
    @Mock private UserClient userClient;
    @Mock private CompanyEventPublisher companyEventPublisher;
    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyDomainService companyDomainService;

    private UUID companyId;
    private UUID hubId;
    private UUID userId;
    private Company company;
    private Location location;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        hubId = UUID.randomUUID();
        userId = UUID.randomUUID();
        location = Location.of("서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0));
        company = Company.create(
                CompanyId.of(companyId),
                HubId.of(hubId),
                "테스트업체",
                CompanyType.PRODUCER,
                location
        );
    }

    @Nested
    @DisplayName("createCompany")
    class CreateCompany {

        @Test
        @DisplayName("MASTER 역할 - 허브 검증 없이 생성 성공")
        void masterRoleSuccess() {
            CreateCompanyCommand command = new CreateCompanyCommand(
                    "MASTER", userId, hubId, "테스트업체", CompanyType.PRODUCER, "서울시 강남구");

            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(hubClient.validateHubExists(hubId)).willReturn(true);
            given(locationClient.resolveLocation("서울시 강남구")).willReturn(location);
            given(companyRepository.save(any())).willReturn(company);

            CompanyResult result = companyService.createCompany(command);

            assertThat(result.name()).isEqualTo("테스트업체");
            assertThat(result.hubId()).isEqualTo(hubId);
            then(companyDomainService).should(never()).validateHubCreateAccess(any(), any());
        }

        @Test
        @DisplayName("HUB_MANAGER 역할 - 허브 접근 검증 후 생성 성공")
        void hubManagerRoleSuccess() {
            CreateCompanyCommand command = new CreateCompanyCommand(
                    "HUB_MANAGER", userId, hubId, "테스트업체", CompanyType.PRODUCER, "서울시 강남구");

            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(hubId, null));
            given(hubClient.validateHubExists(hubId)).willReturn(true);
            given(locationClient.resolveLocation("서울시 강남구")).willReturn(location);
            given(companyRepository.save(any())).willReturn(company);

            companyService.createCompany(command);

            then(companyDomainService).should().validateHubCreateAccess(hubId, hubId);
        }

        @Test
        @DisplayName("유저 없음 - USER_NOT_FOUND")
        void userNotFound() {
            CreateCompanyCommand command = new CreateCompanyCommand(
                    "MASTER", userId, hubId, "테스트업체", CompanyType.PRODUCER, "서울시 강남구");

            given(userClient.getUserInfo(userId)).willReturn(null);

            assertThatThrownBy(() -> companyService.createCompany(command))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("허브 없음 - HUB_NOT_FOUND")
        void hubNotFound() {
            CreateCompanyCommand command = new CreateCompanyCommand(
                    "MASTER", userId, hubId, "테스트업체", CompanyType.PRODUCER, "서울시 강남구");

            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(hubClient.validateHubExists(hubId)).willReturn(false);

            assertThatThrownBy(() -> companyService.createCompany(command))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.HUB_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getCompany")
    class GetCompany {

        @Test
        @DisplayName("조회 성공")
        void success() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));

            CompanyResult result = companyService.getCompany(companyId);

            assertThat(result.companyId()).isEqualTo(companyId);
            assertThat(result.name()).isEqualTo("테스트업체");
        }

        @Test
        @DisplayName("존재하지 않는 업체 - COMPANY_NOT_FOUND")
        void notFound() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.getCompany(companyId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("MASTER 역할 - 수정 성공")
        void masterRoleSuccess() {
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "MASTER", userId, hubId, "수정업체", CompanyType.RECEIVER, "서울시 강남구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(companyRepository.save(any())).willReturn(company);

            CompanyResult result = companyService.update(command);

            assertThat(result.companyId()).isEqualTo(companyId);
            then(locationClient).should(never()).resolveLocation(any()); // 주소 동일 → 좌표 재조회 없음
        }

        @Test
        @DisplayName("HUB_MANAGER 역할 - 허브 접근 검증")
        void hubManagerValidation() {
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "HUB_MANAGER", userId, hubId, "수정업체", CompanyType.RECEIVER, "서울시 강남구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(hubId, null));
            given(companyRepository.save(any())).willReturn(company);

            companyService.update(command);

            then(companyDomainService).should().validateHubAccess(company, hubId);
        }

        @Test
        @DisplayName("COMPANY_MANAGER 역할 - 업체 접근 검증")
        void companyManagerValidation() {
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "COMPANY_MANAGER", userId, hubId, "수정업체", CompanyType.RECEIVER, "서울시 강남구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, companyId));
            given(companyRepository.save(any())).willReturn(company);

            companyService.update(command);

            then(companyDomainService).should().validateCompanyAccess(company, companyId);
        }

        @Test
        @DisplayName("허브 변경 시 허브 존재 검증")
        void hubChangedValidation() {
            UUID newHubId = UUID.randomUUID();
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "MASTER", userId, newHubId, "수정업체", CompanyType.RECEIVER, "서울시 강남구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(hubClient.validateHubExists(newHubId)).willReturn(true);
            given(companyRepository.save(any())).willReturn(company);

            companyService.update(command);

            then(hubClient).should().validateHubExists(newHubId);
        }

        @Test
        @DisplayName("주소 변경 시 좌표 재조회")
        void addressChangedResolvesLocation() {
            Location newLocation = Location.of("부산시 해운대구", BigDecimal.valueOf(35.1), BigDecimal.valueOf(129.1));
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "MASTER", userId, hubId, "수정업체", CompanyType.RECEIVER, "부산시 해운대구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(locationClient.resolveLocation("부산시 해운대구")).willReturn(newLocation);
            given(companyRepository.save(any())).willReturn(company);

            companyService.update(command);

            then(locationClient).should().resolveLocation("부산시 해운대구");
        }

        @Test
        @DisplayName("주소 동일 시 좌표 재조회 없음")
        void addressUnchangedSkipsResolve() {
            UpdateCompanyCommand command = new UpdateCompanyCommand(
                    companyId, "MASTER", userId, hubId, "수정업체", CompanyType.RECEIVER, "서울시 강남구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(companyRepository.save(any())).willReturn(company);

            companyService.update(command);

            then(locationClient).should(never()).resolveLocation(any());
        }
    }

    @Nested
    @DisplayName("deleteCompany")
    class DeleteCompany {

        @Test
        @DisplayName("삭제 성공 - softDelete 호출 및 이벤트 발행")
        void success() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(hubId, null));
            given(orderClient.hasActiveOrders(companyId)).willReturn(false);

            companyService.deleteCompany(companyId, "MASTER", userId);

            assertThat(company.isDeleted()).isTrue();
            assertThat(company.getDeletedAt()).isNotNull();
            then(companyEventPublisher).should().publishCompanyDeleted(any(), any(), any());
        }

        @Test
        @DisplayName("진행 중 주문 존재 - COMPANY_HAS_ACTIVE_ORDERS, 이벤트 미발행")
        void hasActiveOrders() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(null, null));
            given(orderClient.hasActiveOrders(companyId)).willReturn(true);

            assertThatThrownBy(() -> companyService.deleteCompany(companyId, "MASTER", userId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_HAS_ACTIVE_ORDERS);

            then(companyEventPublisher).should(never()).publishCompanyDeleted(any(), any(), any());
        }

        @Test
        @DisplayName("HUB_MANAGER 역할 - 허브 접근 검증")
        void hubManagerValidation() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(userClient.getUserInfo(userId)).willReturn(new UserClient.UserInfo(hubId, null));
            given(orderClient.hasActiveOrders(companyId)).willReturn(false);

            companyService.deleteCompany(companyId, "HUB_MANAGER", userId);

            then(companyDomainService).should().validateHubAccess(company, hubId);
        }

        @Test
        @DisplayName("업체 없음 - COMPANY_NOT_FOUND")
        void companyNotFound() {
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.deleteCompany(companyId, "MASTER", userId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("searchCompanies")
    class SearchCompanies {

        @Test
        @DisplayName("검색 성공")
        void success() {
            SearchCompanyCommand command = new SearchCompanyCommand("테스트", null, null, 0, 10);
            Page<Company> page = new PageImpl<>(List.of(company), PageRequest.of(0, 10), 1);

            given(companyRepository.search("테스트", null, null, PageRequest.of(0, 10))).willReturn(page);

            Page<CompanyResult> result = companyService.searchCompanies(command);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("테스트업체");
        }

        @Test
        @DisplayName("허용되지 않은 size는 DEFAULT(10)로 변환")
        void invalidSizeDefaultsToTen() {
            SearchCompanyCommand command = new SearchCompanyCommand(null, null, null, 0, 7);
            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            given(companyRepository.search(null, null, null, PageRequest.of(0, 10))).willReturn(page);

            companyService.searchCompanies(command);

            then(companyRepository).should().search(null, null, null, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("음수 page는 0으로 변환")
        void negativePageDefaultsToZero() {
            SearchCompanyCommand command = new SearchCompanyCommand(null, null, null, -1, 10);
            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            given(companyRepository.search(null, null, null, PageRequest.of(0, 10))).willReturn(page);

            companyService.searchCompanies(command);

            then(companyRepository).should().search(null, null, null, PageRequest.of(0, 10));
        }
    }
}
