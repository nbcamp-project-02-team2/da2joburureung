package com.da2jobu.deliveryservice.application.deliveryRouteRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 경로 삭제 응답")
public record DeleteDeliveryRouteRecordResponseDto(
        @Schema(description = "삭제 결과 메시지", example = "배송 경로가 삭제되었습니다.")
        String message
) {
    public static DeleteDeliveryRouteRecordResponseDto of(String message) {
        return new DeleteDeliveryRouteRecordResponseDto(message);
    }
}
