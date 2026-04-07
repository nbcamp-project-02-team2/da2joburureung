package com.da2jobu.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Company Service API",
                version = "v1",
                description = "업체 서비스 API 문서"
        )
)
public class OpenApiConfig {
}