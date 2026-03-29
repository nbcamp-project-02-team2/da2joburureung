package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.HubClient;
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
             * todo : 허브 매니저는 본인 담당 허브(hub_id)인지 허브 id가 존재하는지 검증**/
            hubFeignClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("존재하지 않는 허브입니다. hubId: " + hubId);
        } catch (FeignException e) {
            throw new IllegalStateException("허브 서비스 호출 중 오류가 발생했습니다.", e);
        }
    }
}