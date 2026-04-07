package com.da2jobu.deliveryservice.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Delivery Service API",
                version = "v1",
                description = "배송 서비스 API 문서"
        )
)
public class OpenApiConfig {
}