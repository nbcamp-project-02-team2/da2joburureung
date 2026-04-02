package com.delivery.hubpath.interfaces.controller;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.application.service.HubPathApiService;
import com.delivery.hubpath.interfaces.dto.request.CreateHubPathRequest;
import com.delivery.hubpath.interfaces.dto.request.SearchHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hub-paths")
@Tag(name = "HubPath API", description = "허브 경로 관리 API")
public class HubPathController {

    private final HubPathApiService hubPathApiService;

    @PostMapping
    @Operation(summary = "허브 간의 경로 생성",description = "출발 허브이름과 도착 허브이름을 받아 경로를 생성합니다")
    public ResponseEntity<CommonResponse<HubPathResponse>> createHubPath(@RequestBody CreateHubPathRequest request) {

        CreateHubPathCommand command = CreateHubPathCommand.of(request.departHubName(), request.arriveHubName());

        HubPathResponse response = hubPathApiService.createHubPath(command);

        return CommonResponse.created("경로가 성공적으로 생성되었습니다.",response);
    }

    @GetMapping
    @Operation(summary = "허브 간 전체 이동 경로 검색", description = "출발지/도착지 허브 이름으로 경로를 검색합니다.")
    public ResponseEntity<CommonResponse<Page<HubPathResponse>>> getHubPaths(
            @ModelAttribute SearchHubPathRequest searchRequest,
            @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if (pageable.getPageSize() != 10 && pageable.getPageSize() != 30 && pageable.getPageSize() != 50) {
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }

        Page<HubPathResponse> responses = hubPathApiService.searchHubPaths(searchRequest, pageable);

        return CommonResponse.ok(responses);
    }
}
