package com.da2jobu.productservice.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 가격 변동 이력 엔티티 (p_product_price_history 테이블).
 * - 상품 가격이 변경될 때마다 자동으로 이력이 기록됨
 * - 변경 전/후 가격, 변동 사유, 변경자 정보를 저장
 */
@Entity
@Table(name = "p_product_price_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "상품 가격 변동 이력 엔티티")
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "가격 이력 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @Schema(description = "상품")
    private Product product;

    @Column(name = "previous_price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "변경 전 가격", example = "1500.00")
    private BigDecimal previousPrice;

    @Column(name = "new_price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "변경 후 가격", example = "1700.00")
    private BigDecimal newPrice;

    @Column(name = "reason", length = 500)
    @Schema(description = "가격 변경 사유", example = "원자재 가격 인상", nullable = true)
    private String reason;

    @Column(name = "changed_by", nullable = false)
    @Schema(description = "변경자", example = "honggildong")
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    @Builder.Default
    @Schema(description = "변경 시각", example = "2026-04-07T11:00:00")
    private LocalDateTime changedAt = LocalDateTime.now();
}
