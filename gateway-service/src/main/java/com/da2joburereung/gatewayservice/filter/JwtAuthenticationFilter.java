package com.da2joburereung.gatewayservice.filter;

import com.da2joburereung.gatewayservice.util.JwtTokenParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.CommonResponse;
import common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenParser jwtTokenParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        log.info("Gateway request Path={}", path);

        if (isExcludedPath(path)) {
            log.info("Gateway auth excluded Path={}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header null or invalid.");
            return writeErrorResponse(exchange.getResponse(), ErrorCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtTokenParser.parse(token);

            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            log.info("approve request role={}", role);
            log.info("JWT 파싱 userId={}, username={}, role={}", userId, username, role);

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", userId)
                            .header("X-Username", username)
                            .header("X-User-Role", role)
                    )
                    .build();

            return chain.filter(mutatedExchange);

        } catch (ExpiredJwtException e) {
            log.warn("JWT 만료");
            return writeErrorResponse(exchange.getResponse(), ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT invalid: {}", e.getMessage());
            return writeErrorResponse(exchange.getResponse(), ErrorCode.INVALID_TOKEN);
        }
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/api/auth/signup")
                || path.startsWith("/api/auth/login")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/user-service/v3/api-docs")
                || path.startsWith("/company-service/v3/api-docs")
                || path.startsWith("/delivery-service/v3/api-docs")
                || path.startsWith("/hub-service/v3/api-docs")
                || path.startsWith("/hubpath-service/v3/api-docs")
                || path.startsWith("/product-service/v3/api-docs")
                || path.startsWith("/order-service/v3/api-docs");
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, ErrorCode errorCode) {
        response.setStatusCode(errorCode.getStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] body = objectMapper.writeValueAsBytes(CommonResponse.errorBody(errorCode));
            return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
        } catch (Exception e) {
            log.error("Error response fail", e);
            byte[] fallback = "{\"status\":500,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 오류가 발생했습니다.\",\"data\":null}"
                    .getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}