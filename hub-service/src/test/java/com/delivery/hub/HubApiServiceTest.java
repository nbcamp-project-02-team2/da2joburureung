package com.delivery.hub;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.application.dto.UpdateHubCommand;
import com.delivery.hub.application.service.HubApiService;
import com.delivery.hub.domain.model.Hub;
import com.delivery.hub.domain.repository.HubRepository;
import com.delivery.hub.infrastructure.config.Redis.RestPage;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import common.client.KakaoAddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubApiServiceTest {

    @Mock
    private HubRepository hubRepository;

    @Mock
    private KakaoAddressService kakaoAddressService;

    @InjectMocks
    private HubApiService hubApiService;

    @Nested
    @DisplayName("Create Hub Tests")
    class CreateHub {
        @Test
        void createHubSuccess() {
            CreateHubCommand command = new CreateHubCommand("서울 허브", "서울시 강남구");
            given(hubRepository.countActiveHubByName(any())).willReturn(0);
            given(kakaoAddressService.getGeoPoint(any())).willReturn(new KakaoAddressService.GeoPoint(BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0)));

            Hub mockHub = mock(Hub.class);
            given(mockHub.getHubName()).willReturn("서울 허브");
            given(hubRepository.save(any())).willReturn(mockHub);

            HubResponse response = hubApiService.createHub(command, "MASTER", "admin");

            assertThat(response).isNotNull();
            verify(hubRepository, times(1)).save(any());
        }

        @Test
        void createHubFailRole() {
            CreateHubCommand command = new CreateHubCommand("서울 허브", "주소");

            assertThatThrownBy(() -> hubApiService.createHub(command, "USER", "user"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Query Hub Tests")
    class GetHubs {
        @Test
        void getHubsSuccess() {
            SearchHubCommand command = new SearchHubCommand(null, "서울", null);
            Pageable pageable = PageRequest.of(0, 10);
            PageImpl<HubResponse> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

            given(hubRepository.searchHubs(any(), any())).willReturn(page);

            RestPage<HubResponse> result = hubApiService.getHubs(command, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getSize()).isEqualTo(10);
        }

        @Test
        void getHubDetailSuccess() {
            UUID hubId = UUID.randomUUID();
            Hub hub = mock(Hub.class);
            given(hub.getHubId()).willReturn(hubId);
            given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));

            HubResponse response = hubApiService.getHub(hubId);

            assertThat(response).isNotNull();
        }
    }

    @Nested
    @DisplayName("Update Hub Tests")
    class UpdateHub {
        @Test
        void updateHubSuccess() {
            UUID hubId = UUID.randomUUID();
            UpdateHubCommand command = new UpdateHubCommand(hubId, "새 이름", "새 주소");
            Hub hub = mock(Hub.class);

            given(hubRepository.findByHubIdAndDeletedAtIsNull(hubId)).willReturn(Optional.of(hub));
            given(hub.getHubName()).willReturn("기존 이름");
            given(hub.getAddress()).willReturn("기존 주소");
            given(hub.getLatitude()).willReturn(BigDecimal.valueOf(37.0));
            given(hub.getLongitude()).willReturn(BigDecimal.valueOf(127.0));

            given(hubRepository.countActiveHubByName(any())).willReturn(0);
            given(kakaoAddressService.getGeoPoint("새 주소")).willReturn(new KakaoAddressService.GeoPoint(BigDecimal.valueOf(38.0), BigDecimal.valueOf(128.0)));

            hubApiService.updateHub(command, "MASTER", "admin");

            verify(hub).updateHub(eq("새 이름"), eq("새 주소"), any(), any(), eq("admin"));
        }
    }

    @Nested
    @DisplayName("Delete Hub Tests")
    class DeleteHub {
        @Test
        void deleteHubSuccess() {
            UUID hubId = UUID.randomUUID();
            Hub hub = mock(Hub.class);
            given(hubRepository.findById(hubId)).willReturn(Optional.of(hub));
            given(hub.isDeleted()).willReturn(false);

            hubApiService.deleteHub(hubId, "MASTER", "admin");

            verify(hub).softDelete("admin");
        }

        @Test
        void deleteHubFail() {
            UUID hubId = UUID.randomUUID();
            Hub hub = mock(Hub.class);
            given(hubRepository.findById(hubId)).willReturn(Optional.of(hub));
            given(hub.isDeleted()).willReturn(true);

            assertThatThrownBy(() -> hubApiService.deleteHub(hubId, "MASTER", "admin"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}