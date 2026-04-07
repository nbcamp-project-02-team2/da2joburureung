package com.da2jobu.orderservice.interfaces.dto.response;

import com.da2jobu.orderservice.domain.model.Order;
import com.da2jobu.orderservice.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "주문 응답")
public class OrderResponse {

    @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;

    @Schema(description = "공급 업체 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID supplierId;

    @Schema(description = "수령자 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID receiverId;

    @Schema(description = "상품 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID productId;

    @Schema(description = "주문 수량", example = "10")
    private Integer quantity;

    @Schema(description = "주문 시점 단가", example = "15000.00")
    private BigDecimal unitPrice;

    @Schema(description = "배송 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
    private UUID deliveryId;

    @Schema(description = "허브 ID", example = "66666666-6666-6666-6666-666666666666")
    private UUID hubId;

    @Schema(description = "요청 사항", example = "파손 주의", nullable = true)
    private String requirements;

    @Schema(description = "희망 배송일", example = "2026-04-10", nullable = true)
    private LocalDate desiredDeliveryDate;

    @Schema(description = "주문 상태", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "생성 시각", example = "2026-04-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2026-04-07T11:00:00", nullable = true)
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .supplierId(order.getSupplierId())
                .receiverId(order.getReceiverId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .deliveryId(order.getDeliveryId())
                .hubId(order.getHubId())
                .requirements(order.getRequirements())
                .desiredDeliveryDate(order.getDesiredDeliveryDate())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
