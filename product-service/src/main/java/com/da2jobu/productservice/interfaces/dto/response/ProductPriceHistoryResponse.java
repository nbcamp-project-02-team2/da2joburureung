package com.da2jobu.productservice.interfaces.dto.response;

import com.da2jobu.productservice.domain.model.ProductPriceHistory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "상품 가격 변동 이력 응답")
public class ProductPriceHistoryResponse {

    @Schema(description = "가격 이력 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID id;

    @Schema(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID productId;

    @Schema(description = "변경 전 가격", example = "1500.00")
    private BigDecimal previousPrice;

    @Schema(description = "변경 후 가격", example = "1700.00")
    private BigDecimal newPrice;

    @Schema(description = "가격 변경 사유", example = "원자재 가격 인상", nullable = true)
    private String reason;

    @Schema(description = "변경자", example = "honggildong")
    private String changedBy;

    @Schema(description = "변경 시각", example = "2026-04-07T11:00:00")
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
