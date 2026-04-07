package com.delivery.hubpath.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 현재 요청에 포함된 헤더들을 Feign 요청에 그대로 복사
            String authHeader = request.getHeader("Authorization");
            String userRole = request.getHeader("X-User-Role");
            String username = request.getHeader("X-Username");

            if (authHeader != null) template.header("Authorization", authHeader);
            if (userRole != null) template.header("X-User-Role", userRole);
            if (username != null) template.header("X-Username", username);
        }
    }
}