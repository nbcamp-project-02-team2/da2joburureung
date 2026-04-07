package com.da2jobu.orderservice.infrastructure.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "상품 서비스 조회 응답")
public class ProductInfoResponse {

    @Schema(description = "상품 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID id;

    @Schema(description = "상품명", example = "생수 2L")
    private String name;

    @Schema(description = "상품 가격", example = "15000.00")
    private BigDecimal price;

    @Schema(description = "재고 수량", example = "100")
    private Integer stockQuantity;

    @Schema(description = "허브 ID", example = "66666666-6666-6666-6666-666666666666")
    private UUID hubId;

    @Schema(description = "업체 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID companyId;

    @Schema(description = "노출 여부", example = "true")
    private Boolean isVisible;
}
