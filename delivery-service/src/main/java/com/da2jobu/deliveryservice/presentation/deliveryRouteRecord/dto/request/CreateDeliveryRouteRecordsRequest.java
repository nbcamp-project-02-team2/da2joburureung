package com.da2jobu.deliveryservice.presentation.deliveryRouteRecord.dto.request;

import com.da2jobu.deliveryservice.application.deliveryRouteRecord.command.CreateDeliveryRouteRecordsCommand;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "배송 경로 생성 요청")
public record CreateDeliveryRouteRecordsRequest(
        @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "배송 ID는 필수입니다.")
        UUID deliveryId,

        @Schema(description = "배송 경로 목록")
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

    @Schema(description = "배송 경로 항목")
    public record RouteItemRequest(
            @Schema(description = "경로 순번", example = "1")
            @NotNull(message = "배송 경로 순번은 필수입니다.")
            @Min(value = 1, message = "배송 경로 순번은 1 이상이어야 합니다.")
            Integer sequence,

            @Schema(description = "출발지 ID", example = "22222222-2222-2222-2222-222222222222")
            @NotNull(message = "출발지 ID는 필수입니다.")
            UUID originId,

            @Schema(description = "출발지 타입", example = "HUB")
            @NotNull(message = "출발지 타입은 필수입니다.")
            RouteLocationType originType,

            @Schema(description = "도착지 ID", example = "33333333-3333-3333-3333-333333333333")
            @NotNull(message = "도착지 ID는 필수입니다.")
            UUID destinationId,

            @Schema(description = "도착지 타입", example = "COMPANY")
            @NotNull(message = "도착지 타입은 필수입니다.")
            RouteLocationType destinationType,

            @Schema(description = "예상 거리(km)", example = "12.5")
            @NotNull(message = "예상 거리는 필수입니다.")
            BigDecimal expectedDistanceKm,

            @Schema(description = "예상 소요 시간(분)", example = "45")
            @NotNull(message = "예상 소요 시간은 필수입니다.")
            @Min(value = 1, message = "예상 소요 시간은 1분 이상이어야 합니다.")
            Integer expectedDurationMin,

            @Schema(description = "배송 담당자 ID", example = "44444444-4444-4444-4444-444444444444")
            @NotNull(message = "배송 담당자 ID는 필수입니다.")
            UUID deliveryManagerId,

            @Schema(description = "남은 총 예상 시간(분)", example = "90")
            @NotNull(message = "남은 총 예상 시간은 필수입니다.")
            @Min(value = 0, message = "남은 총 예상 시간은 0 이상이어야 합니다.")
            Integer remainDurationMin
    ) {
    }
}
