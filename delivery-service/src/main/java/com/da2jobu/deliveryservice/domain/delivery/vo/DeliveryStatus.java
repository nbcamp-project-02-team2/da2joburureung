package com.da2jobu.deliveryservice.domain.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 상태")
public enum DeliveryStatus {

    @Schema(description = "허브 대기중")
    HUB_WAITING,

    @Schema(description = "허브 이동중")
    HUB_MOVING,

    @Schema(description = "목적지 허브 도착")
    ARRIVED_AT_DESTINATION_HUB,

    @Schema(description = "최종 배송중")
    OUT_FOR_DELIVERY,

    @Schema(description = "배송 완료")
    DELIVERED
}