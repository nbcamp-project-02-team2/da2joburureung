package com.da2jobu.productservice.interfaces.controller;

import com.da2jobu.productservice.application.service.ProductService;
import common.dto.CommonResponse;
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
public class ProductInternalController {

    private final ProductService productService;

    /**
     * 재고 차감 (order-service 내부 호출용).
     */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<CommonResponse<?>> reduceStock(
            @PathVariable UUID productId,
            @RequestParam int quantity) {

        productService.reduceStock(productId, quantity);
        return CommonResponse.ok("재고가 차감되었습니다.");
    }

    /**
     * 재고 복구 (주문 취소 시).
     */
    @PatchMapping("/{productId}/stock/restore")
    public ResponseEntity<CommonResponse<?>> restoreStock(
            @PathVariable UUID productId,
            @RequestParam int quantity) {

        productService.restoreStock(productId, quantity);
        return CommonResponse.ok("재고가 복구되었습니다.");
    }
}
