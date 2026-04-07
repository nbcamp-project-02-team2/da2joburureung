package com.da2jobu.deliveryservice.presentation.delivery;

import com.da2jobu.deliveryservice.application.delivery.dto.CreateDeliveryResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryDetailResponseDto;
import com.da2jobu.deliveryservice.application.delivery.dto.DeliveryListResponseDto;
import com.da2jobu.deliveryservice.application.delivery.service.DeliveryService;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import com.da2jobu.deliveryservice.presentation.delivery.dto.request.CreateDeliveryRequest;
import com.da2jobu.deliveryservice.presentation.delivery.dto.request.UpdateDeliveryStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.da2jobu.deliveryservice.presentation.interceptor.InternalOnly;
import com.da2jobu.deliveryservice.presentation.interceptor.RequireRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Delivery", description = "배송 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "배송 생성(내부)", description = "다른 서비스에서 주문 정보를 기반으로 배송을 생성합니다.")
    // 내부 시스템 전용 (order-service 등) - 일반 사용자 호출 불가
    @PostMapping("/internal/deliveries")
    @InternalOnly
    public CreateDeliveryResponseDto createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        return deliveryService.createDelivery(request.toCommand());
    }

    @Operation(summary = "배송 단건 조회", description = "배송 ID로 배송 상세 정보를 조회합니다.")
    @GetMapping("/deliveries/{deliveryId}")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER", "COMPANY_MANAGER"})
    public DeliveryDetailResponseDto getDelivery(
            @Parameter(description = "조회할 배송 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryId,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return deliveryService.getDelivery(deliveryId, requesterId, requesterRole);
    }

    @Operation(summary = "배송 목록 조회", description = "주문 ID, 상태, 출발/도착 허브 조건으로 배송 목록을 조회합니다.")
    @GetMapping("/deliveries")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER", "COMPANY_MANAGER"})
    public DeliveryListResponseDto getDeliveries(
            @Parameter(description = "주문 ID", example = "22222222-2222-2222-2222-222222222222")
            @RequestParam(required = false) UUID orderId,
            @Parameter(description = "배송 상태", example = "HUB_WAITING")
            @RequestParam(required = false) DeliveryStatus status,
            @Parameter(description = "출발 허브 ID", example = "33333333-3333-3333-3333-333333333333")
            @RequestParam(required = false) UUID originHubId,
            @Parameter(description = "도착 허브 ID", example = "44444444-4444-4444-4444-444444444444")
            @RequestParam(required = false) UUID destinationHubId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return deliveryService.getDeliveries(
                orderId, status, originHubId, destinationHubId,
                page, size, requesterId, requesterRole
        );
    }

    @Operation(summary = "배송 상태 변경", description = "배송 상태를 변경합니다.")
    @PutMapping("/deliveries/{deliveryId}/status")
    @RequireRoles({"MASTER", "HUB_MANAGER", "DELIVERY_MANAGER"})
    public DeliveryDetailResponseDto updateDeliveryStatus(
            @Parameter(description = "상태를 변경할 배송 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return deliveryService.updateDeliveryStatus(deliveryId, request.toCommand(), requesterId, requesterRole);
    }

    @Operation(summary = "배송 삭제", description = "삭제 요청자를 기록하며 배송을 삭제합니다.")
    @DeleteMapping("/deliveries/{deliveryId}")
    @RequireRoles({"MASTER", "HUB_MANAGER"})
    public void deleteDelivery(
            @Parameter(description = "삭제할 배송 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID deliveryId,
            @Parameter(description = "삭제 요청자", example = "master01")
            @RequestParam String deletedBy,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        deliveryService.deleteDelivery(deliveryId, deletedBy, requesterId, requesterRole);
    }
}