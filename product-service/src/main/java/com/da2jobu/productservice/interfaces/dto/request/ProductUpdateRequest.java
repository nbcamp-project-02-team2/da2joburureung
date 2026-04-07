package com.da2jobu.productservice.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 수정 요청 DTO.
 * - 모든 필드가 선택값이며, null인 필드는 수정하지 않음
 */
@Getter
@NoArgsConstructor
@Schema(description = "상품 수정 요청")
public class ProductUpdateRequest {

    @Size(max = 255, message = "상품명은 255자 이내여야 합니다.")
    @Schema(description = "상품명", example = "생수 2L 묶음", nullable = true)
    private String name;

    @DecimalMin(value = "0.00", inclusive = false, message = "가격은 0보다 커야 합니다.")
    @Digits(integer = 13, fraction = 2, message = "가격 형식이 올바르지 않습니다.")
    @Schema(description = "상품 가격", example = "1700.00", nullable = true)
    private BigDecimal price;

    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    @Schema(description = "재고 수량", example = "80", nullable = true)
    private Integer stockQuantity;

    @Schema(description = "상품 노출 여부", example = "true", nullable = true)
    private Boolean isVisible;

    @Schema(description = "상품 설명", example = "행사용 할인 상품입니다.", nullable = true)
    private String description;

    @Size(max = 500, message = "가격 변경 사유는 500자 이내여야 합니다.")
    @Schema(description = "가격 변경 사유", example = "원자재 가격 인상", nullable = true)
    private String reason;
}
