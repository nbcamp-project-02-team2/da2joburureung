package com.da2jobu.productservice.domain.model;

import common.entity.BaseEntity;
import common.exception.CustomException;
import common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "상품 엔티티")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    @Schema(description = "상품명", example = "생수 2L")
    private String name;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "상품 가격", example = "1500.00")
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Schema(description = "재고 수량", example = "100")
    private Integer stockQuantity;

    @Column(name = "hub_id", nullable = false)
    @Schema(description = "소속 허브 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID hubId;

    @Column(name = "company_id", nullable = false)
    @Schema(description = "소속 업체 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID companyId;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    @Schema(description = "상품 노출 여부", example = "true")
    private Boolean isVisible = true;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "상품 설명", example = "대용량 생수 상품입니다.", nullable = true)
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Schema(description = "가격 변동 이력 목록")
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
     * 재고 차감.
     */
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new CustomException(ErrorCode.INVALID_STOCK_QUANTITY);
        }
        if (this.stockQuantity < quantity) {
            throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stockQuantity -= quantity;
    }

    /**
     * 재고 복구 (주문 취소 시).
     */
    public void restoreStock(int quantity) {
        if (quantity <= 0) {
            throw new CustomException(ErrorCode.INVALID_STOCK_QUANTITY);
        }
        this.stockQuantity += quantity;
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
