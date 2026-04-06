package com.da2jobu.aiservice.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        String productId,
        String companyId,
        String hubId,
        String productName,
        BigDecimal price,
        int stockQuantity,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}
