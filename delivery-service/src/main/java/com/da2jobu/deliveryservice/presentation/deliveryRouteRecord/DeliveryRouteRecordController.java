package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto.*;
import com.da2jobu.deliveryservice.application.deliveryRouteRecord.service.DeliveryRouteRecordService;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.CreateDeliveryRouteRecordsRequest;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.UpdateDeliveryRouteMetricsRequest;
import com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request.UpdateDeliveryRouteStatusRequest;
import common.dto.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DeliveryRouteRecordController {

    private final DeliveryRouteRecordService deliveryRouteRecordService;

    @PostMapping("/internal/delivery-routes")
    public ResponseEntity<CommonResponse<CreateDeliveryRouteRecordsResponseDto>> createDeliveryRouteRecords(
            @Valid @RequestBody CreateDeliveryRouteRecordsRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.createDeliveryRouteRecords(request.toCommand())
        );
    }

    @GetMapping("/delivery-routes/{routeRecordId}")
    public ResponseEntity<CommonResponse<DeliveryRouteRecordDetailResponseDto>> getDeliveryRouteRecord(
            @PathVariable UUID routeRecordId
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.getDeliveryRouteRecord(routeRecordId)
        );
    }

    @GetMapping("/deliveries/{deliveryId}/routes")
    public ResponseEntity<CommonResponse<DeliveryRouteRecordListResponseDto>> getDeliveryRouteRecords(
            @PathVariable UUID deliveryId
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.getDeliveryRouteRecords(deliveryId)
        );
    }

    @PutMapping("/delivery-routes/{routeRecordId}/status")
    public ResponseEntity<CommonResponse<UpdateDeliveryRouteStatusResponseDto>> updateDeliveryRouteStatus(
            @PathVariable UUID routeRecordId,
            @Valid @RequestBody UpdateDeliveryRouteStatusRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.updateDeliveryRouteStatus(routeRecordId, request.toCommand())
        );
    }

    @PutMapping("/delivery-routes/{routeRecordId}/metrics")
    public ResponseEntity<CommonResponse<UpdateDeliveryRouteMetricsResponseDto>> updateDeliveryRouteMetrics(
            @PathVariable UUID routeRecordId,
            @Valid @RequestBody UpdateDeliveryRouteMetricsRequest request
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.updateDeliveryRouteMetrics(routeRecordId, request.toCommand())
        );
    }

    @DeleteMapping("/delivery-routes/{routeRecordId}")
    public ResponseEntity<CommonResponse<DeleteDeliveryRouteRecordResponseDto>> deleteDeliveryRouteRecord(
            @PathVariable UUID routeRecordId,
            @RequestParam String deletedBy
    ) {
        return CommonResponse.ok(
                deliveryRouteRecordService.deleteDeliveryRouteRecord(routeRecordId, deletedBy)
        );
    }
}