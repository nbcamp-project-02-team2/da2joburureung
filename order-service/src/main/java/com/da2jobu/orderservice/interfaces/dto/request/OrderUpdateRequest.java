package com.da2jobu.orderservice.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Schema(description = "주문 수정 요청")
public class OrderUpdateRequest {

    @Schema(description = "공급 업체 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
    private UUID supplierId;

    @Schema(description = "수령자 ID", example = "33333333-3333-3333-3333-333333333333", nullable = true)
    private UUID receiverId;

    @Schema(description = "상품 ID", example = "44444444-4444-4444-4444-444444444444", nullable = true)
    private UUID productId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    @Schema(description = "주문 수량", example = "20", nullable = true)
    private Integer quantity;

    @Schema(description = "요청 사항", example = "오전 중 배송 희망", nullable = true)
    private String requirements;

    @Schema(description = "희망 배송일", example = "2026-04-12T10:30:00", nullable = true)
    private LocalDateTime desiredDeliveryAt;

}
