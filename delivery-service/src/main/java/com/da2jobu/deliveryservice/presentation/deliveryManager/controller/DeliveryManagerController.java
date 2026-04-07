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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "배송 담당자 생성", description = "MASTER 또는 HUB_MANAGER 권한으로 배송 담당자를 생성합니다.")
    @PostMapping
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> createDeliveryManager(
            @Valid @RequestBody CreateDeliveryManagerRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
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

    @Operation(summary = "배송 담당자 단건 조회", description = "배송 담당자 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> getDeliveryManager(
            @Parameter(description = "조회할 배송 담당자 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryManagerId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "DELIVERY_MANAGER")
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        DeliveryManagerResult result = deliveryManagerService.getDeliveryManager(deliveryManagerId, requesterId, requesterRole);
        return CommonResponse.ok(DeliveryManagerResponse.from(result));
    }

    @Operation(summary = "배송 담당자 수정", description = "배송 담당자의 허브 또는 타입 정보를 수정합니다.")
    @PatchMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<DeliveryManagerResponse>> updateDeliveryManager(
            @Parameter(description = "수정할 배송 담당자 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryManagerId,
            @RequestBody UpdateDeliveryManagerRequest request,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        UpdateDeliveryManagerCommand command = new UpdateDeliveryManagerCommand(
                deliveryManagerId, request.hubId(), request.type(), requesterId, requesterRole
        );
        DeliveryManagerResult result = deliveryManagerService.updateDeliveryManagers(command);
        return CommonResponse.ok(DeliveryManagerResponse.from(result));
    }

    @Operation(summary = "배송 담당자 삭제", description = "배송 담당자를 삭제합니다.")
    @DeleteMapping("/{deliveryManagerId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public ResponseEntity<CommonResponse<?>> deleteDeliveryManager(
            @Parameter(description = "삭제할 배송 담당자 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryManagerId,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        deliveryManagerService.deleteDeliveryManager(deliveryManagerId, requesterId,requesterRole);
        return CommonResponse.noContent();
    }

    @Operation(summary = "배송 담당자 목록 조회", description = "타입, 허브 ID 조건으로 배송 담당자 목록을 조회합니다.")
    @GetMapping
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<Page<DeliveryManagerResponse>>> searchDeliveryManagers(
            @Parameter(description = "배송 담당자 타입", example = "HUB_DELIVERY")
            @RequestParam(required = false) DeliveryManagerType type,
            @Parameter(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222")
            @RequestParam(required = false) UUID hubId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        SearchDeliveryManagerCommand command = new SearchDeliveryManagerCommand(
                type, hubId, page, size, requesterId, requesterRole
        );
        Page<DeliveryManagerResponse> result = deliveryManagerService.searchDeliveryManagers(command)
                .map(DeliveryManagerResponse::from);
        return CommonResponse.ok("배송 담당자 목록 조회 완료", result);
    }

    @Operation(summary = "배송 담당자 배정 이력 조회", description = "배송 담당자의 배정 이력을 상태 조건으로 조회합니다.")
    @GetMapping("/{deliveryManagerId}/assignments")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public ResponseEntity<CommonResponse<Page<DeliveryAssignmentResponse>>> searchDeliveryAssignments(
            @Parameter(description = "조회할 배송 담당자 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryManagerId,
            @Parameter(description = "배정 상태", example = "ASSIGNED")
            @RequestParam(required = false) DeliveryAssignmentStatus status,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "요청 사용자 ID", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @RequestHeader("X-User-Id") UUID requesterId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
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