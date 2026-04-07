package com.da2jobu.productservice.interfaces.controller;

import com.da2jobu.productservice.application.service.ProductService;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 재고 관련 내부 API 컨트롤러.
 * - /api/internal/** 경로는 gateway에서 라우팅하지 않으므로 외부에 노출되지 않음
 * - order-service → product-service 서비스 간 직접 호출 전용
 */
@RestController
@RequestMapping("/api/internal/products")
@RequiredArgsConstructor
@Tag(name = "Product Internal", description = "상품 내부 연동 API")
public class ProductInternalController {

    private final ProductService productService;

    /**
     * 재고 차감 (order-service 내부 호출용).
     */
    @Operation(summary = "재고 차감", description = "order-service 내부 호출용으로 상품 재고를 차감합니다.")
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<CommonResponse<?>> reduceStock(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID productId,
            @Parameter(description = "차감 수량", example = "5")
            @RequestParam int quantity) {

        productService.reduceStock(productId, quantity);
        return CommonResponse.ok("재고가 차감되었습니다.");
    }

    /**
     * 재고 복구 (주문 취소 시).
     */
    @Operation(summary = "재고 복구", description = "주문 취소 시 상품 재고를 복구합니다.")
    @PatchMapping("/{productId}/stock/restore")
    public ResponseEntity<CommonResponse<?>> restoreStock(
            @Parameter(description = "상품 ID", example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID productId,
            @Parameter(description = "복구 수량", example = "5")
            @RequestParam int quantity) {

        productService.restoreStock(productId, quantity);
        return CommonResponse.ok("재고가 복구되었습니다.");
    }
}
