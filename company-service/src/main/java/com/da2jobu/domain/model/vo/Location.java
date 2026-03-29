package com.da2jobu.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static Location of(String address, BigDecimal latitude, BigDecimal longitude) {
        validate(address, latitude, longitude);
        Location location = new Location();
        location.address = address;
        location.latitude = latitude;
        location.longitude = longitude;
        return location;
    }

    private static void validate(String address, BigDecimal latitude, BigDecimal longitude) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다.");
        }
        if (latitude == null || latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 사이여야 합니다.");
        }
        if (longitude == null || longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 사이여야 합니다.");
        }
        //주소 == 위경도 검증
    }
}