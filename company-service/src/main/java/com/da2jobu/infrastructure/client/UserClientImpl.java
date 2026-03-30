package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.UserClient;
import common.exception.CustomException;
import common.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserFeignClient userFeignClient;

    @Override
    public void validateUserForCompany(UUID userId) {
        try {
            userFeignClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        } catch (FeignException.Conflict e) {
            throw new CustomException(ErrorCode.MANAGER_ALREADY_ASSIGNED);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.USER_SERVICE_ERROR);
        }
    }
}