package com.delivery.hubpath;

import com.delivery.hubpath.application.service.HubPathApiService;
import com.delivery.hubpath.interfaces.controller.HubPathController;
import com.delivery.hubpath.interfaces.dto.request.CreateHubPathRequest;
import com.delivery.hubpath.interfaces.dto.request.UpdateHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HubPathController.class)
@AutoConfigureMockMvc(addFilters = false)
class HubPathApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HubPathApiService hubPathApiService;

    @MockitoBean
    private com.delivery.hubpath.domain.service.HubPathService hubPathService;

    @MockitoBean
    private com.delivery.hubpath.domain.repository.HubPathRepository hubPathRepository;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    @MockitoBean
    private common.client.KakaoAddressService kakaoAddressService;

    private HubPathResponse createMockResponse(UUID departId, UUID arriveId) {
        return new HubPathResponse(
                UUID.randomUUID(), departId, "출발 허브", arriveId, "도착 허브",
                null, BigDecimal.valueOf(10.5), 100, "system",
                LocalDateTime.now(), null, null, null, null
        );
    }

    @Test
    @DisplayName("경로 생성 성공 - MASTER 권한")
    void createHubPathSuccess1() throws Exception {
        UUID departId = UUID.randomUUID();
        UUID arriveId = UUID.randomUUID();
        CreateHubPathRequest request = new CreateHubPathRequest(departId, arriveId);

        when(hubPathApiService.createHubPath(any(), eq("MASTER"), anyString()))
                .thenReturn(createMockResponse(departId, arriveId));

        mockMvc.perform(post("/api/internal/hub-paths")
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("경로 생성 실패 - HUB_MANAGER 권한 불가 (500)")
    void createHubPathFail1() throws Exception {
        CreateHubPathRequest request = new CreateHubPathRequest(UUID.randomUUID(), UUID.randomUUID());

        when(hubPathApiService.createHubPath(any(), eq("HUB_MANAGER"), anyString()))
                .thenThrow(new RuntimeException("권한 부족"));

        mockMvc.perform(post("/api/internal/hub-paths")
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("목록 조회 성공 - 전체 검색")
    void getHubPathsSuccess1() throws Exception {
        when(hubPathApiService.searchHubPaths(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/internal/hub-paths"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("상세 조회 성공 - ID 기반")
    void getHubPathSuccess1() throws Exception {
        UUID id = UUID.randomUUID();
        when(hubPathApiService.getHubPathDetail(eq(id), any(), any()))
                .thenReturn(createMockResponse(UUID.randomUUID(), UUID.randomUUID()));

        mockMvc.perform(get("/api/internal/hub-paths/{hubPathId}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("경로 수정 성공 - MASTER 권한")
    void patchHubPathSuccess1() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateHubPathRequest request = new UpdateHubPathRequest(UUID.randomUUID(), UUID.randomUUID());

        when(hubPathApiService.updateHubPath(any(), eq("MASTER"), anyString()))
                .thenReturn(createMockResponse(request.departHubId(), request.arriveHubId()));

        mockMvc.perform(patch("/api/internal/hub-paths/{hubPathId}", id)
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("경로 수정 실패 - HUB_MANAGER 권한 불가 (500)")
    void patchHubPathFail1() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateHubPathRequest request = new UpdateHubPathRequest(UUID.randomUUID(), UUID.randomUUID());

        when(hubPathApiService.updateHubPath(any(), eq("HUB_MANAGER"), anyString()))
                .thenThrow(new RuntimeException("권한 부족"));

        mockMvc.perform(patch("/api/internal/hub-paths/{hubPathId}", id)
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("경로 삭제 성공 - MASTER 권한")
    void deleteHubPathSuccess1() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(hubPathApiService).deleteHubPath(eq(id), eq("MASTER"), anyString());

        mockMvc.perform(delete("/api/internal/hub-paths/{hubPathId}", id)
                        .header("X-User-Role", "MASTER")
                        .header("X-Username", "admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("경로 삭제 실패 - HUB_MANAGER 삭제 불가 (500)")
    void deleteHubPathFail1() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new RuntimeException("권한 부족"))
                .when(hubPathApiService).deleteHubPath(eq(id), eq("HUB_MANAGER"), anyString());

        mockMvc.perform(delete("/api/internal/hub-paths/{hubPathId}", id)
                        .header("X-User-Role", "HUB_MANAGER")
                        .header("X-Username", "manager"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("경로 삭제 실패 - 헤더 정보 부족 (X-Username 누락)")
    void deleteHubPathFail2() throws Exception {
        mockMvc.perform(delete("/api/internal/hub-paths/{hubPathId}", UUID.randomUUID())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isInternalServerError());
    }
}