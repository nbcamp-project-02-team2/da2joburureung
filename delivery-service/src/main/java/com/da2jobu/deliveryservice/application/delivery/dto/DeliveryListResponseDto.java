package com.da2jobu.deliveryservice.application.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "배송 목록 응답")
public record DeliveryListResponseDto(
        @Schema(description = "배송 목록")
        List<DeliverySummaryResponseDto> deliveries,
        @Schema(description = "전체 데이터 수", example = "25")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size
) {
    public static DeliveryListResponseDto from(Page<DeliverySummaryResponseDto> pageResult) {
        return new DeliveryListResponseDto(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber(),
                pageResult.getSize()
        );
    }
}
