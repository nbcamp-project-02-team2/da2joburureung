package com.da2jobu.orderservice.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상태")
public enum OrderStatus {

    @Schema(description = "주문 접수")
    PENDING,

    @Schema(description = "주문 승인")
    ACCEPTED,

    @Schema(description = "주문 취소")
    CANCELLED,

    @Schema(description = "주문 완료")
    COMPLETED
}