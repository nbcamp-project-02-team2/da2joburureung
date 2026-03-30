package com.delivery.hub.interfaces.controller;

import com.delivery.hub.application.dto.CreateHubCommand;
import com.delivery.hub.interfaces.dto.Request.CreateHubRequest;
import com.delivery.hub.interfaces.dto.Respone.CreateHubResponse;
import common.dto.CommonResponse;
import com.delivery.hub.application.service.HubApiService;
import com.delivery.hub.domain.model.Hub;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<CommonResponse<CreateHubResponse>> createHub(@RequestBody @Valid CreateHubRequest request) {

        CreateHubCommand command = CreateHubCommand.of(request.hub_name(),request.address());

        CreateHubResponse response = hubApiService.createHub(command);

        return CommonResponse.created("허브가 성공적으로 생성되었습니다.", response);
    }

}