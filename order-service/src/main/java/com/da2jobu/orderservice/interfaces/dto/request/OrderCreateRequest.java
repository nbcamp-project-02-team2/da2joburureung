package com.da2jobu.orderservice.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "주문 생성 요청")
public class OrderCreateRequest {

    @NotNull(message = "공급 업체 ID는 필수입니다.")
    @Schema(description = "공급 업체 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID supplierId;

    @NotNull(message = "수령자 ID는 필수입니다.")
    @Schema(description = "수령자 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID receiverId;

    @NotNull(message = "상품 ID는 필수입니다.")
    @Schema(description = "상품 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID productId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    @Schema(description = "주문 수량", example = "10")
    private Integer quantity;

    @Schema(description = "요청 사항", example = "파손 주의", nullable = true)
    private String requirements;

    @Schema(description = "희망 배송일", example = "2026-04-10", nullable = true)
    private LocalDate desiredDeliveryDate;
}
