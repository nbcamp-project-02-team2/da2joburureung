package com.da2jobu.deliveryservice.infrastructure.delivery.client;

import com.da2jobu.deliveryservice.infrastructure.delivery.dto.OrderInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{orderId}")
    OrderInfoDto getOrder(@PathVariable("orderId") UUID orderId);
}
