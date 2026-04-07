package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "배송 담당자 배정 이력 결과")
public record DeliveryAssignmentResult(
        @Schema(description = "배정 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryAssignmentId,

        @Schema(description = "배송 경로 기록 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID deliveryRouteRecordId,

        @Schema(description = "배송 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID deliveryId,

        @Schema(description = "배정 상태", example = "ASSIGNED")
        DeliveryAssignmentStatus status,

        @Schema(description = "생성 시각", example = "2026-04-06T14:30:00")
        LocalDateTime createdAt
) {
    public static DeliveryAssignmentResult from(DeliveryAssignment assignment) {
        return new DeliveryAssignmentResult(
                assignment.getDeliveryAssignmentId().getDeliveryAssignmentId(),
                assignment.getDeliveryRouteRecordId().getDeliveryRouteRecordId(),
                assignment.getDeliveryId().getDeliveryId(),
                assignment.getStatus(),
                assignment.getCreatedAt()
        );
    }
}

