package com.da2jobu.deliveryservice.application.delivery.dto;

import com.da2jobu.deliveryservice.domain.delivery.entity.Delivery;
import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "배송 상세 응답")
public record DeliveryDetailResponseDto(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID deliveryId,
        @Schema(description = "주문 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID orderId,
        @Schema(description = "배송 상태", example = "READY")
        DeliveryStatus status,
        @Schema(description = "출발 허브 ID", example = "33333333-3333-3333-3333-333333333333")
        UUID originHubId,
        @Schema(description = "도착 허브 ID", example = "44444444-4444-4444-4444-444444444444")
        UUID destinationHubId,
        @Schema(description = "배송지 주소", example = "서울특별시 강남구 테헤란로 123")
        String deliveryAddress,
        @Schema(description = "수령인 이름", example = "홍길동")
        String receiverName,
        @Schema(description = "수령인 슬랙 ID", example = "U12345678")
        String receiverSlackId,
        @Schema(description = "업체 배송 담당자 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
        UUID companyDeliveryManagerId,
        @Schema(description = "요청사항", example = "문 앞에 놓아주세요.", nullable = true)
        String requestNote,
        @Schema(description = "예상 총 소요 시간(분)", example = "120")
        Integer expectedDurationTotalMin,
        @Schema(description = "희망 배송 시각", example = "2026-04-06T18:30:00")
        LocalDateTime desiredDeliveryAt,
        @Schema(description = "배송 시작 시각", example = "2026-04-06T17:00:00", nullable = true)
        LocalDateTime startedAt,
        @Schema(description = "배송 완료 시각", example = "2026-04-06T18:20:00", nullable = true)
        LocalDateTime completedAt
) {
    public static DeliveryDetailResponseDto from(Delivery delivery) {
        return new DeliveryDetailResponseDto(
                delivery.getDeliveryId(),
                delivery.getOrderId(),
                delivery.getStatus(),
                delivery.getOriginHubId(),
                delivery.getDestinationHubId(),
                delivery.getDeliveryAddress(),
                delivery.getReceiverName(),
                delivery.getReceiverSlackId(),
                delivery.getCompanyDeliveryManagerId(),
                delivery.getRequestNote(),
                delivery.getExpectedDurationTotalMin(),
                delivery.getDesiredDeliveryAt(),
                delivery.getStartedAt(),
                delivery.getCompletedAt()
        );
    }
}
