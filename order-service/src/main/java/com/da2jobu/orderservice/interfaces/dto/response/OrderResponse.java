package com.da2jobu.orderservice.interfaces.dto.response;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {

    private UUID id;
    private UUID supplierId;
    private UUID receiverId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private UUID deliveryId;
    private UUID hubId;
    private String requirements;
    private LocalDate desiredDeliveryDate;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .supplierId(order.getSupplierId())
                .receiverId(order.getReceiverId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .deliveryId(order.getDeliveryId())
                .hubId(order.getHubId())
                .requirements(order.getRequirements())
                .desiredDeliveryDate(order.getDesiredDeliveryDate())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
