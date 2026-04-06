package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.ProductInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductInfoDto getProduct(@PathVariable("productId") UUID productId);
}
