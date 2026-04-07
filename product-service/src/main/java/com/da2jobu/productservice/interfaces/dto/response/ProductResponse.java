package com.da2jobu.productservice.interfaces.dto.response;

import com.da2jobu.productservice.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "상품 응답")
public class ProductResponse {

    @Schema(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(description = "상품명", example = "생수 2L")
    private String name;

    @Schema(description = "상품 가격", example = "1500.00")
    private BigDecimal price;

    @Schema(description = "재고 수량", example = "100")
    private Integer stockQuantity;

    @Schema(description = "소속 허브 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID hubId;

    @Schema(description = "소속 업체 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID companyId;

    @Schema(description = "상품 설명", example = "대용량 생수 상품입니다.", nullable = true)
    private String description;

    @Schema(description = "상품 노출 여부", example = "true")
    private Boolean isVisible;

    @Schema(description = "생성 시각", example = "2026-04-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2026-04-07T11:00:00", nullable = true)
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
