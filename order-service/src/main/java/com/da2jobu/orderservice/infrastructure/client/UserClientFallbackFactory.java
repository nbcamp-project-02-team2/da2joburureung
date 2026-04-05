package com.da2jobu.orderservice.infrastructure.client;

import common.dto.CommonResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("UserClient fallback 실행. 원인: {}", cause.getMessage());
        return new UserClient() {
            @Override
            public CommonResponse<UserInfoResponse> getMyInfo(String userId) {
                throw new CustomException(ErrorCode.USER_SERVICE_UNAVAILABLE);
            }
        };
    }
}
