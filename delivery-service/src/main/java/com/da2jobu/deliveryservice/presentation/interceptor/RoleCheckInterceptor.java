package com.da2jobu.deliveryservice.presentation.interceptor;

import common.exception.CustomException;
import common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 역할 기반 접근 제어 인터셉터.
 *
 * 내부 API(@InternalOnly) 보호 방식:
 *   1차 방어: X-User-Role 헤더가 있으면 Gateway를 경유한 사용자 요청이므로 거부한다.
 *            (Gateway는 인증된 요청에 항상 X-User-Role을 주입한다.)
 *   2차 방어: INTERNAL_API_SECRET이 설정된 경우, X-Internal-Secret 헤더가 일치해야 한다.
 *            (서비스 간 직접 호출 시 호출 측 서비스가 이 헤더를 추가해야 한다.)
 *
 * 한계:
 *   - Gateway가 X-Internal-Secret 헤더를 외부 요청에서 제거하지 않으므로,
 *     secret을 아는 외부인이 Gateway를 우회해 직접 서비스에 접근하면 차단할 수 없다.
 *   - 최종 보안은 서비스를 외부 네트워크에 노출하지 않는 인프라 격리(Docker network, K8s NetworkPolicy 등)에 의존한다.
 *   - 완전한 보안이 필요하면 mTLS 또는 Gateway에서 X-Internal-Secret 헤더 제거(strip) 설정을 추가해야 한다.
 */
public class RoleCheckInterceptor implements HandlerInterceptor {

    static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final String internalApiSecret;

    public RoleCheckInterceptor(String internalApiSecret) {
        this.internalApiSecret = internalApiSecret;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // ── @InternalOnly 엔드포인트 처리 ─────────────────────────────────────
        InternalOnly internalOnly = handlerMethod.getMethodAnnotation(InternalOnly.class);
        if (internalOnly != null) {
            // 1차: Gateway를 통한 사용자 요청 차단 (X-User-Role 존재 = 인증된 사용자 요청)
            if (request.getHeader("X-User-Role") != null) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            // 2차: Pre-shared secret 검증 (secret이 설정된 경우에만 적용)
            if (internalApiSecret != null && !internalApiSecret.isBlank()) {
                String clientSecret = request.getHeader(INTERNAL_SECRET_HEADER);
                if (!internalApiSecret.equals(clientSecret)) {
                    throw new CustomException(ErrorCode.FORBIDDEN);
                }
            }
            return true;
        }

        // ── @RequireRoles 엔드포인트 처리 ────────────────────────────────────
        RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
        if (requireRoles == null) {
            return true;
        }

        String role = request.getHeader("X-User-Role");
        if (role == null || Arrays.stream(requireRoles.value()).noneMatch(role::equals)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return true;
    }
}