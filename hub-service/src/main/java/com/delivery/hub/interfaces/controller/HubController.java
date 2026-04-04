package com.delivery.hub.interfaces.controller;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.infrastructure.config.Redis.RestPage;
import com.delivery.hub.interfaces.dto.Request.CreateHubRequest;
import com.delivery.hub.interfaces.dto.Request.SearchHubRequest;
import com.delivery.hub.interfaces.dto.Request.UpdateHubRequest;
import com.delivery.hub.interfaces.dto.Respone.HubResponse;
import common.dto.CommonResponse;
import com.delivery.hub.application.service.HubApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hubs")
@Tag(name = "Hub API", description = "허브 관리 API")
public class HubController {

    private final HubApiService hubApiService;

    @PostMapping
    @Operation(summary = "신규 허브 저장", description = "새로운 허브를 저장합니다.")
    public ResponseEntity<CommonResponse<HubResponse>> createHub(@RequestBody @Valid CreateHubRequest request) {

        CreateHubCommand command = CreateHubCommand.of(request.hub_name(),request.address());

        HubResponse response = hubApiService.createHub(command);

        return CommonResponse.created("허브가 성공적으로 생성되었습니다.", response);
    }

    @GetMapping
    @Operation(summary = "허브 조회 및 검색", description = "전체 허브를 조회하거나 검색합니다")
    public ResponseEntity<CommonResponse<Page<HubResponse>>> getHubs(
            @ModelAttribute SearchHubRequest searchRequest,
            @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        SearchHubCommand command = SearchHubCommand.of(searchRequest.hub_name(),searchRequest.address());

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),

                    10,
                    pageable.getSort()
            );
        }

        RestPage<HubResponse> response = hubApiService.getHubs(command, pageable);

        return CommonResponse.ok(response.toPage());
    }

    @GetMapping("/{hub_id}")
    @Operation(summary = "특정 허브 검색", description = "특정 허브의 상세 정보를 받아옵니다")
    public ResponseEntity<CommonResponse<HubResponse>> getHub(@Valid @PathVariable("hub_id") UUID hubId) {

        HubResponse response = hubApiService.getHub(hubId);

        return CommonResponse.ok(response);
    }

    @PatchMapping("/{hub_id}")
    @Operation(summary = "특정 허브 수정", description = "특정 허브의 내용을 수정합니다")
    public ResponseEntity<CommonResponse<HubResponse>> updateHub(@Valid @RequestBody UpdateHubRequest request, @PathVariable UUID hub_id) {

        HubResponse response = hubApiService.updateHub(hub_id, request);

        return CommonResponse.ok(response);
    }

    @DeleteMapping("/{hub_id}")
    @Operation(summary = "허브 삭제", description = "허브를 삭제합니다")
    public ResponseEntity<CommonResponse<?>> deleteHub(@PathVariable("hub_id") UUID hub_id) {
        hubApiService.deleteHub(hub_id);

        return CommonResponse.noContent();
    }

    @GetMapping("/all") // 최종 경로: GET /api/hubs/all
    @Operation(summary = "알고리즘용 전체 허브 조회", description = "삭제되지 않은 모든 허브를 리스트로 반환합니다.")
    public ResponseEntity<CommonResponse<List<HubResponse>>> getAllHubsForAlgo() {
        List<HubResponse> allHubs = hubApiService.findAllActiveHubs();
        return CommonResponse.ok(allHubs);
    }
}