package com.da2jobu.orderservice.domain.model;

import common.entity.BaseEntity;
import common.exception.CustomException;
import common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "p_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLDelete(sql = "UPDATE p_order SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Schema(description = "주문 엔티티")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Column(name = "supplier_id", nullable = false)
    @Schema(description = "공급 업체 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID supplierId;

    @Column(name = "receiver_id", nullable = false)
    @Schema(description = "수령자 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID receiverId;

    @Column(name = "product_id", nullable = false)
    @Schema(description = "상품 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    @Schema(description = "주문 수량", example = "10")
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    @Schema(description = "주문 시점 단가", example = "15000.00", nullable = true)
    private BigDecimal unitPrice;

    @Column(name = "delivery_id")
    @Schema(description = "배송 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
    private UUID deliveryId;

    @Column(name = "hub_id", nullable = false)
    @Schema(description = "허브 ID", example = "66666666-6666-6666-6666-666666666666")
    private UUID hubId;

    @Column(name = "requirements", columnDefinition = "TEXT")
    @Schema(description = "요청 사항", example = "파손 주의", nullable = true)
    private String requirements;

    @Column(name = "desired_delivery_date")
    @Schema(description = "희망 배송일", example = "2026-04-10", nullable = true)
    private LocalDate desiredDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "주문 상태", example = "PENDING")
    private OrderStatus status;

    public void accept() {
        if (!OrderStatus.PENDING.equals(this.status)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.ACCEPTED;
    }

    public void cancel() {
        if (!OrderStatus.PENDING.equals(this.status) && !OrderStatus.ACCEPTED.equals(this.status)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void complete() {
        if (!OrderStatus.ACCEPTED.equals(this.status)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void assignDelivery(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void update(UUID supplierId, UUID receiverId, UUID productId,
                       Integer quantity, String requirements, LocalDate desiredDeliveryDate) {
        if (supplierId != null) this.supplierId = supplierId;
        if (receiverId != null) this.receiverId = receiverId;
        if (productId != null) this.productId = productId;
        if (quantity != null) this.quantity = quantity;
        if (requirements != null) this.requirements = requirements;
        if (desiredDeliveryDate != null) this.desiredDeliveryDate = desiredDeliveryDate;
    }
}
