package com.delivery.hubpath;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.application.dto.UpdateHubPathCommand;
import com.delivery.hubpath.application.service.HubPathApiService;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.domain.service.HubPathService;
import com.delivery.hubpath.infrastructure.client.HubClient;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import com.delivery.hubpath.infrastructure.client.PageResponse;
import common.dto.CommonResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubPathApiServiceTest {

    @InjectMocks
    private HubPathApiService hubPathApiService;

    @Mock
    private HubClient hubClient;

    @Mock
    private HubPathRepository hubPathRepository;

    @Mock
    private HubPathService hubPathService;

    private HubResponse createHubResponse(UUID id, String name) {
        return new HubResponse(id, name, "주소", BigDecimal.valueOf(37.5), BigDecimal.valueOf(127.0));
    }

    private PageResponse<HubResponse> createPageResponse(List<HubResponse> content) {
        return PageResponse.<HubResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(content.size())
                .totalPages(1)
                .last(true)
                .build();
    }

    @Test
    @DisplayName("경로 생성 성공 - MASTER 권한 및 정상 허브 데이터")
    void createHubPathSuccess1() {
        UUID departId = UUID.randomUUID();
        UUID arriveId = UUID.randomUUID();
        CreateHubPathCommand command = new CreateHubPathCommand(departId, arriveId);
        PageResponse<HubResponse> pageResponse = createPageResponse(List.of(createHubResponse(departId, "출발")));

        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok(pageResponse).getBody());
        when(hubClient.getAllHubs()).thenReturn(CommonResponse.ok(Collections.<HubResponse>emptyList()).getBody());
        when(hubPathService.createAndSavePath(any(), any(), any())).thenReturn(mock(HubPath.class));

        assertDoesNotThrow(() -> hubPathApiService.createHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 생성 성공 - 모든 허브 목록 활용")
    void createHubPathSuccess2() {
        CreateHubPathCommand command = new CreateHubPathCommand(UUID.randomUUID(), UUID.randomUUID());
        HubResponse hub = createHubResponse(UUID.randomUUID(), "허브");
        PageResponse<HubResponse> pageResponse = createPageResponse(List.of(hub));

        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok(pageResponse).getBody());
        when(hubClient.getAllHubs()).thenReturn(CommonResponse.ok(List.of(hub)).getBody());
        when(hubPathService.createAndSavePath(any(), any(), any())).thenReturn(mock(HubPath.class));

        assertDoesNotThrow(() -> hubPathApiService.createHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 생성 실패 - MASTER 권한 아님 (403)")
    void createHubPathFail1() {
        CreateHubPathCommand command = new CreateHubPathCommand(UUID.randomUUID(), UUID.randomUUID());
        assertThrows(ResponseStatusException.class, () -> hubPathApiService.createHubPath(command, "HUB_MANAGER", "manager"));
    }

    @Test
    @DisplayName("경로 생성 실패 - 허브 검색 결과 없음 (content empty)")
    void createHubPathFail2() {
        CreateHubPathCommand command = new CreateHubPathCommand(UUID.randomUUID(), UUID.randomUUID());
        PageResponse<HubResponse> emptyPage = createPageResponse(Collections.emptyList());
        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok(emptyPage).getBody());

        assertThrows(EntityNotFoundException.class, () -> hubPathApiService.createHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 수정 성공 - MASTER 권한 및 유효 ID")
    void updateHubPathSuccess1() {
        UUID pathId = UUID.randomUUID();
        UpdateHubPathCommand command = new UpdateHubPathCommand(pathId, UUID.randomUUID(), UUID.randomUUID());
        HubPath existingPath = mock(HubPath.class);
        PageResponse<HubResponse> pageResponse = createPageResponse(List.of(createHubResponse(UUID.randomUUID(), "허브")));

        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(existingPath));
        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok(pageResponse).getBody());
        when(hubClient.getAllHubs()).thenReturn(CommonResponse.ok(Collections.<HubResponse>emptyList()).getBody());
        when(hubPathService.createAndSavePath(any(), any(), any())).thenReturn(mock(HubPath.class));

        assertDoesNotThrow(() -> hubPathApiService.updateHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 수정 성공 - 파라미터 미입력 시 기존 ID 활용")
    void updateHubPathSuccess2() {
        UUID pathId = UUID.randomUUID();
        UpdateHubPathCommand command = new UpdateHubPathCommand(pathId, null, null);
        HubPath existingPath = mock(HubPath.class);
        PageResponse<HubResponse> pageResponse = createPageResponse(List.of(createHubResponse(UUID.randomUUID(), "기존허브")));

        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(existingPath));
        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok(pageResponse).getBody());
        when(hubClient.getAllHubs()).thenReturn(CommonResponse.ok(Collections.<HubResponse>emptyList()).getBody());
        when(hubPathService.createAndSavePath(any(), any(), any())).thenReturn(mock(HubPath.class));

        assertDoesNotThrow(() -> hubPathApiService.updateHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 수정 실패 - 존재하지 않는 경로 ID")
    void updateHubPathFail1() {
        UpdateHubPathCommand command = new UpdateHubPathCommand(UUID.randomUUID(), null, null);
        when(hubPathRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> hubPathApiService.updateHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 수정 실패 - 허브 목록 API 조회 데이터 누락 (500 처리 대상)")
    void updateHubPathFail2() {
        UUID pathId = UUID.randomUUID();
        UpdateHubPathCommand command = new UpdateHubPathCommand(pathId, null, null);
        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(mock(HubPath.class)));
        when(hubClient.getHubs(any(), anyInt(), anyInt())).thenReturn(CommonResponse.ok((PageResponse<HubResponse>)null).getBody());

        assertThrows(EntityNotFoundException.class, () -> hubPathApiService.updateHubPath(command, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 삭제 성공 - 정상 소프트 딜리트")
    void deleteHubPathSuccess1() {
        UUID pathId = UUID.randomUUID();
        HubPath hubPath = mock(HubPath.class);
        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(hubPath));
        when(hubPath.isDeleted()).thenReturn(false);

        assertDoesNotThrow(() -> hubPathApiService.deleteHubPath(pathId, "MASTER", "admin"));
        verify(hubPath).softDelete("admin");
    }

    @Test
    @DisplayName("경로 삭제 성공 - MASTER 권한 확인")
    void deleteHubPathSuccess2() {
        UUID pathId = UUID.randomUUID();
        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(mock(HubPath.class)));

        assertDoesNotThrow(() -> hubPathApiService.deleteHubPath(pathId, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 삭제 실패 - 이미 삭제된 경로 재삭제 시도")
    void deleteHubPathFail1() {
        UUID pathId = UUID.randomUUID();
        HubPath hubPath = mock(HubPath.class);
        when(hubPathRepository.findById(pathId)).thenReturn(Optional.of(hubPath));
        when(hubPath.isDeleted()).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> hubPathApiService.deleteHubPath(pathId, "MASTER", "admin"));
    }

    @Test
    @DisplayName("경로 삭제 실패 - 존재하지 않는 경로")
    void deleteHubPathFail2() {
        when(hubPathRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> hubPathApiService.deleteHubPath(UUID.randomUUID(), "MASTER", "admin"));
    }
}