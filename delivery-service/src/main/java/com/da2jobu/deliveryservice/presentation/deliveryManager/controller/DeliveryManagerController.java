package com.da2jobu.deliveryservice.presentation.deliveryManager.controller;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.CreateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.SearchDeliveryAssignmentCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.SearchDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.command.UpdateDeliveryManagerCommand;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.result.DeliveryManagerResult;
import com.da2jobu.deliveryservice.application.deliveryManager.service.DeliveryManagerService;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import com.da2jobu.deliveryservice.presentation.deliveryManager.dto.request.CreateDeliveryManagerRequest;
import com.da2jobu.deliveryservice.presentation.deliveryManager.dto.request.UpdateDeliveryManagerRequest;
import com.da2jobu.deliveryservice.presentation.deliveryManager.dto.response.DeliveryAssignmentResponse;
import com.da2jobu.deliveryservice.presentation.deliveryManager.dto.response.DeliveryManagerResponse;
import com.da2jobu.deliveryservice.presentation.interceptor.RequireRoles;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @RequireRoles({"MASTER", "HUB_MANAGER"})
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

    @GetMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> getDeliveryManager(
            @PathVariable UUID deliveryManagerId,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        DeliveryManagerResult result = deliveryManagerService.getDeliveryManager(deliveryManagerId, requesterId, requesterRole);
        return CommonResponse.ok(DeliveryManagerResponse.from(result));
    }

    @PatchMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> updateDeliveryManager(
            @PathVariable UUID deliveryManagerId,
            @RequestBody UpdateDeliveryManagerRequest request,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        UpdateDeliveryManagerCommand command = new UpdateDeliveryManagerCommand(
                deliveryManagerId, request.hubId(), request.type(), requesterId, requesterRole
        );
        DeliveryManagerResult result = deliveryManagerService.updateDeliveryManagers(command);
        return CommonResponse.ok(DeliveryManagerResponse.from(result));
    }

    @DeleteMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<?>> deleteDeliveryManager(
            @PathVariable UUID deliveryManagerId,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        deliveryManagerService.deleteDeliveryManager(deliveryManagerId, requesterId,requesterRole);
        return CommonResponse.noContent();
    }

    @GetMapping
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<Page<DeliveryManagerResponse>>> searchDeliveryManagers(
            @RequestParam(required = false) DeliveryManagerType type,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        SearchDeliveryManagerCommand command = new SearchDeliveryManagerCommand(
                type, hubId, page, size, requesterId, requesterRole
        );
        Page<DeliveryManagerResponse> result = deliveryManagerService.searchDeliveryManagers(command)
                .map(DeliveryManagerResponse::from);
        return CommonResponse.ok("배송 담당자 목록 조회 완료", result);
    }

    @GetMapping("/{deliveryManagerId}/assignments")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<Page<DeliveryAssignmentResponse>>> searchDeliveryAssignments(
            @PathVariable UUID deliveryManagerId,
            @RequestParam(required = false) DeliveryAssignmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        SearchDeliveryAssignmentCommand command = new SearchDeliveryAssignmentCommand(
                deliveryManagerId, status, page, size, requesterId, requesterRole
        );
        Page<DeliveryAssignmentResponse> result = deliveryManagerService.searchDeliveryAssignments(command)
                .map(DeliveryAssignmentResponse::from);
        return CommonResponse.ok("배정 이력 조회 완료", result);
    }
}