package com.da2jobu.productservice.infrastructure.client;

import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("UserClient fallback 실행. 원인: {}", cause.getMessage());
        return new UserClient() {
            @Override
            public UUID getHubIdByUserId(UUID userId) {
                throw new CustomException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public UUID getCompanyIdByUserId(UUID userId) {
                throw new CustomException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            }
        };
    }
}
