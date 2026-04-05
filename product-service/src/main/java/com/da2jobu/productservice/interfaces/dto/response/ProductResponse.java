package com.da2jobu.productservice.interfaces.dto.response;

import com.da2jobu.productservice.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 응답 DTO.
 * - 단건 조회, 목록 조회, 생성/수정 결과 반환 시 사용
 */
@Getter
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private UUID hubId;
    private UUID companyId;
    private String description;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Product 엔티티 -> 응답 DTO 변환
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .hubId(product.getHubId())
                .companyId(product.getCompanyId())
                .description(product.getDescription())
                .isVisible(product.getIsVisible())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
