package com.da2jobu.infrastructure.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubClient implements com.da2jobu.application.service.HubClient {

    private final HubFeignClient hubFeignClient;

    @Override
    public void validateExists(UUID hubId) {
        try {
            hubFeignClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("존재하지 않는 허브입니다. hubId: " + hubId);
        } catch (FeignException e) {
            throw new IllegalStateException("허브 서비스 호출 중 오류가 발생했습니다.", e);
        }
    }
}