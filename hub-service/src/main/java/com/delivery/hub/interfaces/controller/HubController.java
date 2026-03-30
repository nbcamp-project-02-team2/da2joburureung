package com.delivery.hub.interfaces.controller;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.application.dto.SearchHubCommand;
import com.delivery.hub.interfaces.dto.Request.CreateHubRequest;
import com.delivery.hub.interfaces.dto.Request.SearchHubRequest;
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

        Page<HubResponse> response = hubApiService.getHubs(command, pageable);

        return CommonResponse.ok(response);
    }
}