package com.da2jobu.infrastructure.client;

import com.da2jobu.application.client.LocationClient;
import com.da2jobu.domain.model.vo.Location;
import common.client.KakaoAddressService;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationClientImpl implements LocationClient {

    private final KakaoAddressService kakaoAddressService;

    @Override
    public Location resolveLocation(String address) {
        try {
            KakaoAddressService.GeoPoint geoPoint = kakaoAddressService.getGeoPoint(address);
            log.info("주소 위경도 확인 : latitude={}, longitude={}", geoPoint.latitude(), geoPoint.longitude());
            return Location.of(address, geoPoint.latitude(), geoPoint.longitude());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ADDRESS);
        }
    }
}
