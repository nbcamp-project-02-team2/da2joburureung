package com.da2jobu.orderservice.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderUpdateRequest {

    private UUID supplierId;

    private UUID receiverId;

    private UUID productId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    private String requirements;

    private LocalDate desiredDeliveryDate;
}
