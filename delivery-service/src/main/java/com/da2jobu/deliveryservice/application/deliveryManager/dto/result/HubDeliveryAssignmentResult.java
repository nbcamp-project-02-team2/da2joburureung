package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryAssignment;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "허브 배송 배정 결과")
public record HubDeliveryAssignmentResult(
        @Schema(description = "배정 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryAssignmentId,

        @Schema(description = "배송 담당자 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID deliveryManagerId,

        @Schema(description = "담당자 순번", example = "1")
        int managerSeq
) {
    public static HubDeliveryAssignmentResult from(DeliveryAssignment assignment, DeliveryManager manager) {
        return new HubDeliveryAssignmentResult(
                assignment.getDeliveryAssignmentId().getDeliveryAssignmentId(),
                manager.getDeliveryManagerId().getDeliveryManagerId(),
                manager.getSeq()
        );
    }
}
