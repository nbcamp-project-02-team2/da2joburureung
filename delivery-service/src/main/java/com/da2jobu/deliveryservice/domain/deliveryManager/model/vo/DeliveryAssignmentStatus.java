package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 배정 상태")
public enum DeliveryAssignmentStatus {

    @Schema(description = "배정 완료")
    ASSIGNED,

    @Schema(description = "배송 완료")
    COMPLETED
}