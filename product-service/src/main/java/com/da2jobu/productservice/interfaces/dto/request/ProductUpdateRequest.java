package com.da2jobu.productservice.interfaces.dto.request;

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
public class ProductUpdateRequest {

    @Size(max = 255, message = "상품명은 255자 이내여야 합니다.")
    private String name;

    @DecimalMin(value = "0.00", inclusive = false, message = "가격은 0보다 커야 합니다.")
    @Digits(integer = 13, fraction = 2, message = "가격 형식이 올바르지 않습니다.")
    private BigDecimal price;

    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;

    private Boolean isVisible;

    private String description;

    /** 가격 변경 시 변동 사유 (예: "원자재 가격 인상", "할인 이벤트") */
    @Size(max = 500, message = "가격 변경 사유는 500자 이내여야 합니다.")
    private String reason;
}
