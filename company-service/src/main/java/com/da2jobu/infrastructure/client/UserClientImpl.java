package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.UserClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserFeignClient userFeignClient;

    @Override
    public void validateUserExistsAndRole(UUID userId) {
        try {
            userFeignClient.getUser(userId);
            /*todo : 유저의 롤이 업체 담당자 인지 검증로직 추가*/
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. userId: " + userId);
        } catch (FeignException e) {
            throw new IllegalStateException("유저 서비스 호출 중 오류가 발생했습니다.", e);
        }
    }
}