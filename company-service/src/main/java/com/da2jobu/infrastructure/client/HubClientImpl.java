package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.HubClient;
import common.exception.CustomException;
import common.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubClientImpl implements HubClient {

    private final HubFeignClient hubFeignClient;

    @Override
    public void validateHubExists(UUID hubId) {
        try {
            /**
             * todo : 허브 매니저는 본인 담당 허브(hub_id)인지 허브 id가 존재하는지 검증
             */
            hubFeignClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(ErrorCode.HUB_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(ErrorCode.HUB_SERVICE_ERROR);
        }
    }
}