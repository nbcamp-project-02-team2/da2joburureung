package com.da2jobu.deliveryservice.presentation.delivery.dto.request;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "배송 생성 요청")
public record CreateDeliveryRequest(
        @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @Schema(description = "출발 허브 ID", example = "22222222-2222-2222-2222-222222222222")
        @NotNull(message = "출발 허브 ID는 필수입니다.")
        UUID originHubId,

        @Schema(description = "도착 허브 ID", example = "33333333-3333-3333-3333-333333333333")
        @NotNull(message = "목적지 허브 ID는 필수입니다.")
        UUID destinationHubId,

        @Schema(description = "배송지 주소", example = "서울특별시 강남구 테헤란로 123")
        @NotBlank(message = "배송지 주소는 필수입니다.")
        String deliveryAddress,

        @Schema(description = "수령인 이름", example = "홍길동")
        @NotBlank(message = "수령인 이름은 필수입니다.")
        String receiverName,

        @Schema(description = "수령인 슬랙 ID", example = "U12345678")
        @NotBlank(message = "수령인 Slack ID는 필수입니다.")
        String receiverSlackId,

        UUID supplierCompanyId,

        UUID receiverCompanyId,

        @Schema(description = "업체 배송 담당자 ID", example = "44444444-4444-4444-4444-444444444444", nullable = true)
        UUID companyDeliveryManagerId,

        @Schema(description = "요청사항", example = "문 앞에 놓아주세요.", nullable = true)
        String requestNote,

        @Schema(description = "예상 총 소요 시간(분)", example = "120")
        @NotNull(message = "예상 총 소요 시간은 필수입니다.")
        @Min(value = 1, message = "예상 총 소요 시간은 1분 이상이어야 합니다.")
        Integer expectedDurationTotalMin,

        @Schema(description = "희망 배송 시각", example = "2026-04-06T18:30:00")
        @NotNull(message = "희망 배송 시각은 필수입니다.")
        LocalDateTime desiredDeliveryAt
) {
    public CreateDeliveryCommand toCommand() {
        return new CreateDeliveryCommand(
                orderId,
                originHubId,
                destinationHubId,
                deliveryAddress,
                receiverName,
                receiverSlackId,
                supplierCompanyId,
                receiverCompanyId,
                companyDeliveryManagerId,
                requestNote,
                expectedDurationTotalMin,
                null,
                desiredDeliveryAt,
                null,
                null
        );
    }
}
