package com.da2jobu.productservice.interfaces.dto.response;

import com.da2jobu.productservice.domain.model.ProductPriceHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 가격 변동 이력 응답 DTO.
 */
@Getter
@Builder
public class ProductPriceHistoryResponse {

    private UUID id;
    private UUID productId;
    private BigDecimal previousPrice;
    private BigDecimal newPrice;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;

    // ProductPriceHistory 엔티티 -> 응답 DTO 변환
    public static ProductPriceHistoryResponse from(ProductPriceHistory history) {
        return ProductPriceHistoryResponse.builder()
                .id(history.getId())
                .productId(history.getProduct().getId())
                .previousPrice(history.getPreviousPrice())
                .newPrice(history.getNewPrice())
                .reason(history.getReason())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }
}
