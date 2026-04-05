package com.da2jobu.orderservice.infrastructure.client;

import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error("ProductClient fallback 실행. 원인: {}", cause.getMessage());
        return new ProductClient() {
            @Override
            public CommonResponse<ProductInfoResponse> getProduct(UUID productId) {
                throw new CustomException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
            }

            @Override
            public CommonResponse<Void> reduceStock(UUID productId, int quantity) {
                throw new CustomException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
            }

            @Override
            public CommonResponse<Void> restoreStock(UUID productId, int quantity) {
                throw new CustomException(ErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
            }
        };
    }
}
