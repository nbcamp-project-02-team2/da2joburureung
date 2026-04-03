package com.da2jobu.deliveryservice.presentation.delivery.dto.request;

import com.da2jobu.deliveryservice.application.delivery.command.CreateDeliveryCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDeliveryRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "출발 허브 ID는 필수입니다.")
        UUID originHubId,

        @NotNull(message = "목적지 허브 ID는 필수입니다.")
        UUID destinationHubId,

        @NotBlank(message = "배송지 주소는 필수입니다.")
        String deliveryAddress,

        @NotBlank(message = "수령인 이름은 필수입니다.")
        String receiverName,

        @NotBlank(message = "수령인 Slack ID는 필수입니다.")
        String receiverSlackId,

        @NotNull(message = "업체 배송 담당자 ID는 필수입니다.")
        UUID companyDeliveryManagerId,

        String requestNote,

        @NotNull(message = "예상 총 소요 시간은 필수입니다.")
        @Min(value = 1, message = "예상 총 소요 시간은 1분 이상이어야 합니다.")
        Integer expectedDurationTotalMin
) {
    public CreateDeliveryCommand toCommand() {
        return new CreateDeliveryCommand(
                orderId,
                originHubId,
                destinationHubId,
                deliveryAddress,
                receiverName,
                receiverSlackId,
                companyDeliveryManagerId,
                requestNote,
                expectedDurationTotalMin,
                null,
                null,
                null
        );
    }
}
