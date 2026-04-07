package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

@Schema(description = "배송 담당자 유휴 시간 정보")
public record ManagerIdleDuration(
        @Schema(description = "배송 담당자 ID")
        DeliveryManagerId managerId,

        @Schema(description = "유휴 시간", example = "PT30M")
        Duration idleDuration           //경과시간
) {
}

