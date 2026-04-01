package com.delivery.hubpath.domain.model;

import com.delivery.hubpath.infrastructure.client.HubResponse;
import common.client.KakaoAddressService;
import common.client.KakaoAddressService.GeoPoint;
import common.client.KakaoAddressService.RouteSummary;
import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_hub_path")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HubPath extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "hub_path_id", updatable = false, nullable = false)
    private UUID hub_path_id;

    @Column(name = "depart_hub_id", nullable = false)
    private UUID departHubId;

    @Column(name = "depart_hub_name", nullable = false)
    private String departHubName;

    @Column(name = "arrive_hub_id", nullable = false)
    private UUID arriveHubId;

    @Column(name = "arrive_hub_name", nullable = false)
    private String arriveHubName;

    @Column(name = "middle_hub_id")
    private UUID middleHubId;

    @Column(name = "middle_hub_name")
    private String middleHubName;

    @Column(name = "distance", nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    public static HubPath createPath(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs, KakaoAddressService kakaoService) {
        HubPath hubPath = new HubPath();
        hubPath.updatePath(depart, arrive, allHubs, kakaoService);
        return hubPath;
    }

    public void updatePath(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs, KakaoAddressService kakaoService) {

        double directDist = calculateHaversine(
                depart.getLatitude().doubleValue(), depart.getLongitude().doubleValue(),
                arrive.getLatitude().doubleValue(), arrive.getLongitude().doubleValue()
        );

        HubResponse middleHub = null;
        GeoPoint waypoint = null;

        if (directDist >= 170.0) {
            middleHub = findMiddleHub(depart, arrive, allHubs);
            waypoint = new GeoPoint(middleHub.getLatitude(), middleHub.getLongitude());
        }

        GeoPoint origin = new GeoPoint(depart.getLatitude(), depart.getLongitude());
        GeoPoint destination = new GeoPoint(arrive.getLatitude(), arrive.getLongitude());
        RouteSummary summary = kakaoService.getRouteSummary(origin, destination, waypoint);

        this.departHubId = depart.getId();
        this.departHubName = depart.getHub_name();
        this.arriveHubId = arrive.getId();
        this.arriveHubName = arrive.getHub_name();
        this.middleHubId = middleHub != null ? middleHub.getId() : null;
        this.middleHubName = middleHub != null ? middleHub.getHub_name() : null;
        this.distance = BigDecimal.valueOf(summary.distanceMeter() / 1000.0);
        this.duration = summary.durationSecond() / 60;
    }

    private HubResponse findMiddleHub(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs) {
        return allHubs.stream()
                .filter(h -> !h.getHub_name().equals(depart.getHub_name()) && !h.getHub_name().equals(arrive.getHub_name()))
                .peek(h -> {
                    double d1 = calculateHaversine(depart.getLatitude().doubleValue(), depart.getLongitude().doubleValue(), h.getLatitude().doubleValue(), h.getLongitude().doubleValue());
                    double d2 = calculateHaversine(h.getLatitude().doubleValue(), h.getLongitude().doubleValue(), arrive.getLatitude().doubleValue(), arrive.getLongitude().doubleValue());
                    System.out.println("허브: " + h.getHub_name() + " | 합계 거리: " + (d1 + d2) + "km (d1:" + d1 + ", d2:" + d2 + ")");
                })
                .min(Comparator.comparingDouble(h -> {
                    double dist1 = calculateHaversine(depart.getLatitude().doubleValue(), depart.getLongitude().doubleValue(), h.getLatitude().doubleValue(), h.getLongitude().doubleValue());
                    double dist2 = calculateHaversine(h.getLatitude().doubleValue(), h.getLongitude().doubleValue(), arrive.getLatitude().doubleValue(), arrive.getLongitude().doubleValue());
                    return dist1 + dist2;
                }))
                .orElseThrow(() -> new IllegalArgumentException("적절한 경유지를 찾을 수 없습니다."));
    }

    private static double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}