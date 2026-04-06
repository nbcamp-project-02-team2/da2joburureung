package com.da2jobu.orderservice.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "공급 업체 ID는 필수입니다.")
    private UUID supplierId;

    @NotNull(message = "수령자 ID는 필수입니다.")
    private UUID receiverId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    private String requirements;

    private LocalDate desiredDeliveryDate;
}
