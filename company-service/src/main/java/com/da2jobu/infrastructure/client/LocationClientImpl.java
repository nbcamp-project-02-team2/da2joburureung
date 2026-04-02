package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.LocationClient;
import com.da2jobu.domain.model.vo.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class LocationClientImpl implements LocationClient {
    @Override
    public Location resolveLocation(String address){
        BigDecimal latitude = BigDecimal.ZERO;
        BigDecimal longitude = BigDecimal.ZERO;
        log.info("[External API] 주소 위경도 확인 : latitude={}, longitude={}", latitude,longitude);

        /* TODO : 지도 API 호출 예정 */
        return Location.of(address,latitude,longitude);
    }
}
