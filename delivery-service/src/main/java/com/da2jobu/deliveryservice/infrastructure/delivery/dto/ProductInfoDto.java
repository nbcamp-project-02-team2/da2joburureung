package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductInfoDto(
        @JsonProperty("product_id")
        UUID productId,

        @JsonProperty("company_id")
        UUID companyId,

        @JsonProperty("hub_id")
        UUID hubId,

        @JsonProperty("name")
        String name,

        @JsonProperty("price")
        BigDecimal price,

        @JsonProperty("stock_quantity")
        Integer stockQuantity,

        @JsonProperty("is_public")
        Boolean isPublic,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}
