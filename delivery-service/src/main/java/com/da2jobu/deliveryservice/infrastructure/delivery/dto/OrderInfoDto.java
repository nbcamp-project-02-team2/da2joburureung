package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderInfoDto(
        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("quantity")
        Integer quantity,

        @JsonProperty("unit_price")
        BigDecimal unitPrice,

        @JsonProperty("total_price")
        BigDecimal totalPrice,

        @JsonProperty("status")
        String status,

        @JsonProperty("supplier_id")
        UUID supplierId,

        @JsonProperty("receiver_id")
        UUID receiverId,

        @JsonProperty("delivery_id")
        UUID deliveryId,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}
