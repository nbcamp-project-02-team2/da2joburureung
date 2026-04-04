package com.da2jobu.productservice.domain.model;

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
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 상품과의 N:1 관계 (외래키: product_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 변경 전 가격
    @Column(name = "previous_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal previousPrice;

    // 변경 후 가격
    @Column(name = "new_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal newPrice;

    // 가격 변동 사유 (예: "원자재 가격 인상", "할인 이벤트")
    @Column(name = "reason", length = 500)
    private String reason;

    // 가격을 변경한 사용자 식별 정보
    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    // 변경 일시
    @Column(name = "changed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
}
