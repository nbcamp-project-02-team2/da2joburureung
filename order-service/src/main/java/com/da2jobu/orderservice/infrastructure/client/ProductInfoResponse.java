package com.da2jobu.orderservice.infrastructure.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProductInfoResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private UUID hubId;
    private UUID companyId;
    private Boolean isVisible;
}
