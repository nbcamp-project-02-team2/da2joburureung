package com.da2jobu.deliverymanagerservice.interfaces.controller;

import com.da2jobu.deliverymanagerservice.application.dto.command.CreateDeliveryManagerCommand;
import com.da2jobu.deliverymanagerservice.application.dto.result.DeliveryManagerResult;
import com.da2jobu.deliverymanagerservice.application.service.DeliveryManagerService;
import com.da2jobu.deliverymanagerservice.interfaces.dto.request.CreateDeliveryManagerRequest;
import com.da2jobu.deliverymanagerservice.interfaces.dto.response.DeliveryManagerResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "DeliveryManager", description = "배송 담당자 관리 API")
@RestController
@RequestMapping("/api/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @PostMapping
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> createDeliveryManager(
            @Valid @RequestBody CreateDeliveryManagerRequest request,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        CreateDeliveryManagerCommand command = new CreateDeliveryManagerCommand(
                request.userId(),
                request.hubId(),
                request.type(),
                requesterId,
                requesterRole
        );

        DeliveryManagerResult result = deliveryManagerService.createDeliveryManager(command);

        return CommonResponse.created("배송 담당자 생성 완료", DeliveryManagerResponse.from(result));
    }
}