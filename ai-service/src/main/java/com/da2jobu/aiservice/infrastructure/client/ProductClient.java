package com.da2jobu.aiservice.infrastructure.client;

import com.da2jobu.aiservice.infrastructure.client.dto.ProductResponse;
import common.dto.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    CommonResponse<ProductResponse> getProduct(
            @PathVariable String productId
    );
}

