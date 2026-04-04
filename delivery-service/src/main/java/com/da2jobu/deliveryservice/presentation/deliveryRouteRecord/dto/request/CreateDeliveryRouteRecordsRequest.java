package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateDeliveryRouteRecordsRequest(
        @NotNull(message = "배송 ID는 필수입니다.")
        UUID deliveryId,

        @NotEmpty(message = "배송 경로 목록은 비어 있을 수 없습니다.")
        List<@Valid RouteItemRequest> routes
) {
    public CreateDeliveryRouteRecordsCommand toCommand() {
        return new CreateDeliveryRouteRecordsCommand(
                deliveryId,
                routes.stream()
                        .map(route -> new CreateDeliveryRouteRecordsCommand.RouteItem(
                                route.sequence(),
                                route.originId(),
                                route.originType(),
                                route.destinationId(),
                                route.destinationType(),
                                route.expectedDistanceKm(),
                                route.expectedDurationMin(),
                                route.deliveryManagerId(),
                                route.remainDurationMin()
                        ))
                        .toList()
        );
    }

    public record RouteItemRequest(
            @NotNull(message = "배송 경로 순번은 필수입니다.")
            @Min(value = 1, message = "배송 경로 순번은 1 이상이어야 합니다.")
            Integer sequence,

            @NotNull(message = "출발지 ID는 필수입니다.")
            UUID originId,

            @NotNull(message = "출발지 타입은 필수입니다.")
            RouteLocationType originType,

            @NotNull(message = "도착지 ID는 필수입니다.")
            UUID destinationId,

            @NotNull(message = "도착지 타입은 필수입니다.")
            RouteLocationType destinationType,

            @NotNull(message = "예상 거리는 필수입니다.")
            BigDecimal expectedDistanceKm,

            @NotNull(message = "예상 소요 시간은 필수입니다.")
            @Min(value = 1, message = "예상 소요 시간은 1분 이상이어야 합니다.")
            Integer expectedDurationMin,

            @NotNull(message = "배송 담당자 ID는 필수입니다.")
            UUID deliveryManagerId,

            @NotNull(message = "남은 총 예상 시간은 필수입니다.")
            @Min(value = 0, message = "남은 총 예상 시간은 0 이상이어야 합니다.")
            Integer remainDurationMin
    ) {
    }
}
