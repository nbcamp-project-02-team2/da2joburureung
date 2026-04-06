package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity.DeliveryRouteRecord;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "배송 경로 상태 변경 응답")
public record UpdateDeliveryRouteStatusResponseDto(
        @Schema(description = "경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID routeRecordId,

        @Schema(description = "변경된 배송 경로 상태", example = "COMPLETED")
        DeliveryRouteStatus status
) {
    public static UpdateDeliveryRouteStatusResponseDto from(DeliveryRouteRecord record) {
        return new UpdateDeliveryRouteStatusResponseDto(
                record.getDeliveryRouteRecordId(),
                record.getStatus()
        );
    }
}
