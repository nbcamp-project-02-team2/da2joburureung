package com.da2jobu.infrastructure.client;

import com.da2jobu.application.service.LocationClient;
import com.da2jobu.domain.model.vo.Location;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LocationClientImpl implements LocationClient {
    @Override
    public Location resolveLocation(String address){
        BigDecimal latitude = BigDecimal.ZERO;
        BigDecimal longitude = BigDecimal.ZERO;
        /* TODO : 지도 API 호출 예정 */
        return Location.of(address,latitude,longitude);
    }
}
