package com.da2jobu.deliveryservice.application.deliveryManager.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "업체 배송 배정 결과")
public record CompanyDeliveryAssignmentResult(
        @Schema(description = "기준 허브 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID hubId,

        @Schema(description = "총 배송 건수", example = "15")
        int totalDeliveries,

        @Schema(description = "배정된 배송 담당자 수", example = "3")
        int assignedManagers,

        @Schema(description = "생성된 경로 수", example = "3")
        int routeCount
) {
    public static CompanyDeliveryAssignmentResult of(UUID hubId, int totalDeliveries,
                                                     int assignedManagers, int routeCount) {
        return new CompanyDeliveryAssignmentResult(hubId, totalDeliveries, assignedManagers, routeCount);
    }
}
