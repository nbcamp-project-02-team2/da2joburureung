package com.da2jobu.orderservice.domain.model;

import common.entity.BaseEntity;
import common.exception.CustomException;
import common.exception.ErrorCode;
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
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // 공급 업체 ID
    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    // 수령자 ID (허브 또는 수신 업체)
    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    // 상품 ID
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    // 주문 수량
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // 단가 (주문 시점의 상품 가격)
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // 배송 ID (DeliveryCreatedEvent 수신 후 업데이트)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    // 공급 업체 소속 허브 ID
    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    // 요청 사항
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    // 수령 희망일
    @Column(name = "desired_delivery_date")
    private LocalDate desiredDeliveryDate;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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
