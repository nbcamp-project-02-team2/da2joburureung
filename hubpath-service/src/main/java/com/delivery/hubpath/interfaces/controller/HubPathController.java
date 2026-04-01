package com.delivery.hubpath.interfaces.controller;

import com.delivery.hubpath.application.dto.CreateHubPathCommand;
import com.delivery.hubpath.application.service.HubPathApiService;
import com.delivery.hubpath.interfaces.dto.request.CreateHubPathRequest;
import com.delivery.hubpath.interfaces.dto.response.HubPathResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hub-paths")
@Tag(name = "HubPath API", description = "허브 경로 관리 API")
public class HubPathController {

    private final HubPathApiService hubPathApiService;

    @PostMapping
    public ResponseEntity<CommonResponse<HubPathResponse>> createHubPath(@RequestBody CreateHubPathRequest request) {

        CreateHubPathCommand command = CreateHubPathCommand.of(request.departHubName(), request.arriveHubName());

        HubPathResponse response = hubPathApiService.createHubPath(command);

        return CommonResponse.created("경로가 성공적으로 생성되었습니다.",response);
    }
}
