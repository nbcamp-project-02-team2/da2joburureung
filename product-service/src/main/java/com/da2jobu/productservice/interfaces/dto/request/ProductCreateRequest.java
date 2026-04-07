package com.da2jobu.productservice.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 상품 생성 요청 DTO.
 * - @Valid 를 통해 필수값 및 형식 검증 수행
 */
@Getter
@NoArgsConstructor
@Schema(description = "상품 생성 요청")
public class ProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 255, message = "상품명은 255자 이내여야 합니다.")
    @Schema(description = "상품명", example = "생수 2L")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0.00", inclusive = false, message = "가격은 0보다 커야 합니다.")
    @Digits(integer = 13, fraction = 2, message = "가격 형식이 올바르지 않습니다.")
    @Schema(description = "상품 가격", example = "1500.00")
    private BigDecimal price;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    @Schema(description = "재고 수량", example = "100")
    private Integer stockQuantity;

    @Schema(description = "소속 허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
    private UUID hubId;

    @Schema(description = "소속 업체 ID", example = "33333333-3333-3333-3333-333333333333", nullable = true)
    private UUID companyId;

    @Schema(description = "상품 설명", example = "대용량 생수 상품입니다.", nullable = true)
    private String description;
}
