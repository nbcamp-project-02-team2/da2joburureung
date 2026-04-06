package com.delivery.hub;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.application.dto.UpdateHubCommand;
import com.delivery.hub.application.service.HubApiService;
import com.delivery.hub.infrastructure.config.QuerydslConfig;
import com.delivery.hub.infrastructure.config.Redis.EmbeddedRedisConfig;
import com.delivery.hub.infrastructure.config.Redis.RestPage;
import com.delivery.hub.interfaces.controller.HubController;
import com.delivery.hub.interfaces.dto.Request.CreateHubRequest;
import com.delivery.hub.interfaces.dto.Request.UpdateHubRequest;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HubController.class)
@AutoConfigureMockMvc(addFilters = false)
class HubApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HubApiService hubApiService;

    @MockitoBean
    private QuerydslConfig querydslConfig;

    @MockitoBean
    private EmbeddedRedisConfig embeddedRedisConfig;

    @MockitoBean
    private common.client.KakaoAddressService kakaoAddressService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    private HubResponse createMockHubResponse(UUID id, String name) {
        return new HubResponse(
                id, name, "서울시 강남구", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0),
                "admin", LocalDateTime.now(), null, null, null, null
        );
    }

    @Test
    @DisplayName("허브 생성 성공 - MASTER 권한")
    void createHubSuccess() throws Exception {
        CreateHubRequest request = new CreateHubRequest("서울 허브", "서울시 강남구");
        HubResponse response = createMockHubResponse(UUID.randomUUID(), "서울 허브");

        when(hubApiService.createHub(any(CreateHubCommand.class), eq("MASTER"), eq("admin")))
                .thenReturn(response);

        mockMvc.perform(post("/api/internal/hubs")
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("허브 생성 실패 - 권한 부족 (500)")
    void createHubFail() throws Exception {
        CreateHubRequest request = new CreateHubRequest("서울 허브", "서울시 강남구");

        when(hubApiService.createHub(any(CreateHubCommand.class), eq("HUB_MANAGER"), anyString()))
                .thenThrow(new RuntimeException("권한 부족"));

        mockMvc.perform(post("/api/internal/hubs")
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("허브 목록 조회 성공")
    void getHubsSuccess() throws Exception {
        List<HubResponse> content = Collections.singletonList(createMockHubResponse(UUID.randomUUID(), "서울 허브"));
        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<HubResponse> page = new PageImpl<>(content, pageRequest, 1);

        RestPage<HubResponse> restPage = new RestPage<>(page);

        when(hubApiService.getHubs(any(SearchHubCommand.class), any()))
                .thenReturn(restPage);

        mockMvc.perform(get("/api/internal/hubs")
                        .param("hub_name", "서울")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 허브 상세 조회 성공")
    void getHubDetailSuccess() throws Exception {
        UUID hubId = UUID.randomUUID();
        when(hubApiService.getHub(hubId)).thenReturn(createMockHubResponse(hubId, "서울 허브"));

        mockMvc.perform(get("/api/internal/hubs/{hub_id}", hubId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허브 수정 성공 - MASTER 권한")
    void updateHubSuccess() throws Exception {
        UUID hubId = UUID.randomUUID();
        UpdateHubRequest request = new UpdateHubRequest("수정 허브", "수정 주소");
        HubResponse response = createMockHubResponse(hubId, "수정 허브");

        when(hubApiService.updateHub(any(UpdateHubCommand.class), eq("MASTER"), eq("admin")))
                .thenReturn(response);

        mockMvc.perform(patch("/api/internal/hubs/{hub_id}", hubId)
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허브 수정 실패 - 권한 부족 (500)")
    void updateHubFail() throws Exception {
        UUID hubId = UUID.randomUUID();
        UpdateHubRequest request = new UpdateHubRequest("수정 허브", "수정 주소");

        when(hubApiService.updateHub(any(UpdateHubCommand.class), eq("HUB_MANAGER"), anyString()))
                .thenThrow(new RuntimeException("권한 부족"));

        mockMvc.perform(patch("/api/internal/hubs/{hub_id}", hubId)
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("허브 삭제 성공 - MASTER 권한")
    void deleteHubSuccess() throws Exception {
        UUID hubId = UUID.randomUUID();
        doNothing().when(hubApiService).deleteHub(eq(hubId), eq("MASTER"), eq("admin"));

        mockMvc.perform(delete("/api/internal/hubs/{hub_id}", hubId)
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("허브 삭제 실패 - 권한 부족 (500)")
    void deleteHubFail() throws Exception {
        UUID hubId = UUID.randomUUID();
        doThrow(new RuntimeException("권한 부족"))
                .when(hubApiService).deleteHub(eq(hubId), eq("HUB_MANAGER"), anyString());

        mockMvc.perform(delete("/api/internal/hubs/{hub_id}", hubId)
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("알고리즘용 전체 허브 조회 성공")
    void getAllHubsForAlgoSuccess() throws Exception {
        when(hubApiService.findAllActiveHubs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/internal/hubs/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("허브 상세 조회 실패 - 존재하지 않는 허브 (500)")
    void getHubDetailFail() throws Exception {
        UUID hubId = UUID.randomUUID();
        when(hubApiService.getHub(hubId)).thenThrow(new RuntimeException("허브 없음"));

        mockMvc.perform(get("/api/internal/hubs/{hub_id}", hubId))
                .andExpect(status().isInternalServerError());
    }
}