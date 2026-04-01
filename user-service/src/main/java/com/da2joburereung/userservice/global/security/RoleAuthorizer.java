package com.da2joburereung.userservice.global.security;

import common.exception.CustomException;
import common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class RoleAuthorizer {
    public void requireMaster(String role) {
        if (!"MASTER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    public void requireAnyOf(String role, String... allowedRoles) {
        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(role)) {
                return;
            }
        }
        throw new CustomException(ErrorCode.FORBIDDEN);
    }
}
