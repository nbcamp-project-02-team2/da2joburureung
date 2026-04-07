package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 담당자 유형")
public enum DeliveryManagerType {

    @Schema(description = "허브 배송 담당자")
    HUB_DELIVERY,

    @Schema(description = "업체 배송 담당자")
    COMPANY_DELIVERY
}