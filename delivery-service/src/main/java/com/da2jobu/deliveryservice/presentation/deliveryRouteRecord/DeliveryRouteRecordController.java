package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto.*;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.service.DeliveryRouteRecordService;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.CreateDeliveryRouteRecordsRequest;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.UpdateDeliveryRouteMetricsRequest;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.UpdateDeliveryRouteStatusRequest;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Delivery Route Record", description = "배송 경로 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeliveryRouteRecordController {

    private final DeliveryRouteRecordService deliveryRouteRecordService;

    @Operation(summary = "배송 경로 생성(내부)", description = "다른 서비스 또는 내부 로직에서 배송 경로 기록을 생성합니다.")
    @PostMapping("/internal/delivery-routes")
    public ResponseEntity<CommonResponse<CreateDeliveryRouteRecordsResponseDto>> createDeliveryRouteRecords(
            @Valid @RequestBody CreateDeliveryRouteRecordsRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.createDeliveryRouteRecords(request.toCommand())
        );
    }

    @Operation(summary = "배송 경로 단건 조회", description = "경로 기록 ID로 배송 경로 상세를 조회합니다.")
    @GetMapping("/delivery-routes/{routeRecordId}")
    public ResponseEntity<CommonResponse<DeliveryRouteRecordDetailResponseDto>> getDeliveryRouteRecord(
            @Parameter(description = "조회할 경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID routeRecordId
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.getDeliveryRouteRecord(routeRecordId)
        );
    }

    @Operation(summary = "배송별 경로 목록 조회", description = "배송 ID로 해당 배송의 전체 경로 목록을 조회합니다.")
    @GetMapping("/deliveries/{deliveryId}/routes")
    public ResponseEntity<CommonResponse<DeliveryRouteRecordListResponseDto>> getDeliveryRouteRecords(
            @Parameter(description = "조회할 배송 ID", example = "22222222-2222-2222-2222-222222222222")
            @PathVariable UUID deliveryId
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.getDeliveryRouteRecords(deliveryId)
        );
    }

    @Operation(summary = "배송 경로 상태 변경", description = "배송 경로 상태를 변경합니다.")
    @PutMapping("/delivery-routes/{routeRecordId}/status")
    public ResponseEntity<CommonResponse<UpdateDeliveryRouteStatusResponseDto>> updateDeliveryRouteStatus(
            @Parameter(description = "상태를 변경할 경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID routeRecordId,
            @Valid @RequestBody UpdateDeliveryRouteStatusRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.updateDeliveryRouteStatus(routeRecordId, request.toCommand())
        );
    }

    @Operation(summary = "배송 경로 실적 갱신", description = "실제 이동 거리, 실제 소요 시간, 남은 시간을 갱신합니다.")
    @PutMapping("/delivery-routes/{routeRecordId}/metrics")
    public ResponseEntity<CommonResponse<UpdateDeliveryRouteMetricsResponseDto>> updateDeliveryRouteMetrics(
            @PathVariable UUID routeRecordId,
            @Valid @RequestBody UpdateDeliveryRouteMetricsRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.updateDeliveryRouteMetrics(routeRecordId, request.toCommand())
        );
    }

    @Operation(summary = "배송 경로 삭제", description = "삭제 요청자를 기록하며 배송 경로를 삭제합니다.")
    @DeleteMapping("/delivery-routes/{routeRecordId}")
    public ResponseEntity<CommonResponse<DeleteDeliveryRouteRecordResponseDto>> deleteDeliveryRouteRecord(
            @Parameter(description = "실적을 갱신할 경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID routeRecordId,
            @Parameter(description = "삭제 요청자", example = "master01")
            @RequestParam String deletedBy
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.deleteDeliveryRouteRecord(routeRecordId, deletedBy)
        );
    }
}