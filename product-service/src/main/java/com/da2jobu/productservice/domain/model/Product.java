package com.da2jobu.productservice.domain.model;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 상품 엔티티 (p_product 테이블).
 * - 논리적 삭제(Soft Delete)
 * - 가격 변경 시 ProductPriceHistory에 자동 이력 기록
 */
@Entity
@Table(name = "p_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLDelete(sql = "UPDATE p_product SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 상품명
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // 상품 가격 (DECIMAL(15,2) — 소수점 2자리까지 저장)
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // 재고 수량
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // 소속 허브 ID (FeignClient로 유효성 검증)
    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    // 소속 업체 ID (FeignClient로 유효성 검증)
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    // 상품 노출 여부 (true: 노출, false: 비노출)
    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    // 상품 설명
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 가격 변동 이력 (1:N 관계, 양방향 매핑)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductPriceHistory> priceHistories = new ArrayList<>();

    /**
     * 상품 정보 수정.
     * - 가격이 변경된 경우 가격 이력을 자동 기록
     */
    public void update(String name, BigDecimal price, Integer stockQuantity,
                       Boolean isVisible, String description, String reason, String changedBy) {
        if (name != null) this.name = name;
        if (stockQuantity != null) this.stockQuantity = stockQuantity;
        if (isVisible != null) this.isVisible = isVisible;
        if (description != null) this.description = description;

        // 가격이 변경된 경우에만 이력 기록
        if (price != null && price.compareTo(this.price) != 0) {
            addPriceHistory(this.price, price, reason, changedBy);
            this.price = price;
        }
    }

    /**
     * 가격 변동 이력 추가.
     */
    private void addPriceHistory(BigDecimal previousPrice, BigDecimal newPrice,
                                 String reason, String changedBy) {
        ProductPriceHistory history = ProductPriceHistory.builder()
                .product(this)
                .previousPrice(previousPrice)
                .newPrice(newPrice)
                .reason(reason)
                .changedBy(changedBy)
                .build();
        this.priceHistories.add(history);
    }
}
