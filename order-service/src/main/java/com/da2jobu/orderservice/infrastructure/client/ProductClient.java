package com.da2jobu.orderservice.infrastructure.client;

import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product-service", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    CommonResponse<ProductInfoResponse> getProduct(@PathVariable("productId") UUID productId);

    @PatchMapping("/api/internal/products/{productId}/stock")
    CommonResponse<Void> reduceStock(@PathVariable("productId") UUID productId,
                                     @RequestParam("quantity") int quantity);

    @PatchMapping("/api/internal/products/{productId}/stock/restore")
    CommonResponse<Void> restoreStock(@PathVariable("productId") UUID productId,
                                      @RequestParam("quantity") int quantity);
}
