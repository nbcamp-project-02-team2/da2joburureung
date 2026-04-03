package com.da2jobu.deliveryservice.application.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record DeliveryListResponseDto(
        List<DeliverySummaryResponseDto> deliveries,
        long totalElements,
        int totalPages,
        int page,
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
