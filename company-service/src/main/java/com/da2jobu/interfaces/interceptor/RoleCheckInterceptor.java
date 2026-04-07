package com.da2jobu.interfaces.interceptor;

import com.da2jobu.interfaces.annotation.RequireRoles;
import common.exception.CustomException;
import common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

public class RoleCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

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