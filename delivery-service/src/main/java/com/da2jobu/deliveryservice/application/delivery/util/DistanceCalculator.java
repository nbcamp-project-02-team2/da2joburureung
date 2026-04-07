package com.da2jobu.deliveryservice.application.delivery.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_SPEED_KMH = 60.0;

    public BigDecimal calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    public int calculateDurationMin(BigDecimal distanceKm) {
        double durationHour = distanceKm.doubleValue() / AVERAGE_SPEED_KMH;
        double durationMin = durationHour * 60.0;

        return (int) Math.ceil(durationMin);
    }
}
