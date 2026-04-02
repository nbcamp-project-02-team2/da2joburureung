package com.delivery.hubpath.domain.model;

import com.delivery.hubpath.infrastructure.client.HubResponse;
import common.client.KakaoAddressService;
import common.client.KakaoAddressService.GeoPoint;
import common.client.KakaoAddressService.RouteSummary;
import common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
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

    @Column(name = "first_distance", precision = 10, scale = 2)
    @Schema(description = "출발 허브에서 중간 경유지까지의 거리 (단위: km)", example = "120.50")
    private BigDecimal firstDistance;

    @Column(name = "first_duration")
    @Schema(description = "출발 허브에서 중간 경유지까지의 소요 시간 (단위: 분)", example = "95")
    private Integer firstDuration;

    @Column(name = "second_distance", precision = 10, scale = 2)
    @Schema(description = "중간 경유지에서 도착 허브까지의 거리 (단위: km)", example = "85.20")
    private BigDecimal secondDistance;

    @Column(name = "second_duration")
    @Schema(description = "중간 경유지에서 도착 허브까지의 소요 시간 (단위: 분)", example = "70")
    private Integer secondDuration;

    @Column(name = "distance", nullable = false, precision = 10, scale = 2)
    @Schema(description = "출발 허브에서 도착 허브까지 총 거리 (단위: km)", example = "205.70")
    private BigDecimal distance;

    @Column(name = "duration", nullable = false)
    @Schema(description = "출발 허브에서 도착 허브까지 총 소요 시간 (단위: 분)", example = "165")
    private Integer duration;

    public static HubPath createPath(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs, KakaoAddressService kakaoService) {
        HubPath hubPath = new HubPath();
        hubPath.updatePath(depart, arrive, allHubs, kakaoService);
        return hubPath;
    }

    public void updatePath(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs, KakaoAddressService kakaoService) {
        GeoPoint origin = new GeoPoint(depart.getLatitude(), depart.getLongitude());
        GeoPoint destination = new GeoPoint(arrive.getLatitude(), arrive.getLongitude());

        // 1차적으로 출발허브와 도착허브 간의 거리 확인
        RouteSummary summary = kakaoService.getRouteSummary(origin, destination, null);

        HubResponse middle = null;
        GeoPoint waypoint = null;

        // 만약 출발허브와 도착허브 간의 거리가 200km가 넘어가면 실행
        if (summary.distanceMeter() > 200000) {
            // 다익스트라 알고리즘을 바탕으로 제일 효율적인 중간 허브 계산
            List<HubResponse> path = findDijkstraPath(depart, arrive, allHubs);

            // 반환된 경로 리스트가 [출발, 경유, 도착] 처럼 3개 이상일 경우 (경유지가 존재하는 경우)
            if (path.size() > 2) {
                middle = path.get(1);
                waypoint = new GeoPoint(middle.getLatitude(), middle.getLongitude());
                summary = kakaoService.getRouteSummary(origin, destination, waypoint);
            }
        }

        this.departHubId = depart.getId();
        this.departHubName = depart.getHub_name();
        this.arriveHubId = arrive.getId();
        this.arriveHubName = arrive.getHub_name();
        this.middleHubId = middle != null ? middle.getId() : null;
        this.middleHubName = middle != null ? middle.getHub_name() : null;

        // 전체 합산
        this.distance = BigDecimal.valueOf(summary.distanceMeter()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        this.duration = summary.durationSecond() / 60;

        // 출발 허브 -> 중간 경유지 / 중간 경유지 -> 도착 허브까지의 각각의 거리와 소요 시간
        if (middle != null) {
            RouteSummary firstLeg = kakaoService.getRouteSummary(origin, waypoint, null);
            RouteSummary secondLeg = kakaoService.getRouteSummary(waypoint, destination, null);

            this.firstDistance = BigDecimal.valueOf(firstLeg.distanceMeter()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            this.firstDuration = firstLeg.durationSecond() / 60;
            this.secondDistance = BigDecimal.valueOf(secondLeg.distanceMeter()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            this.secondDuration = secondLeg.durationSecond() / 60;
        } else {
            this.firstDistance = this.distance;
            this.firstDuration = this.duration;
            this.secondDistance = BigDecimal.ZERO;
            this.secondDuration = 0;
        }
    }

    private List<HubResponse> findDijkstraPath(HubResponse start, HubResponse end, List<HubResponse> allHubs) {
        Map<UUID, Double> dists = new HashMap<>();
        Map<UUID, UUID> prevs = new HashMap<>();
        PriorityQueue<DNode> pq = new PriorityQueue<>();

        // 모든 허브의 최소 거리를 무한대로 초기화
        for (HubResponse h : allHubs) {
            dists.put(h.getId(), Double.MAX_VALUE);
        }

        dists.put(start.getId(), 0.0);
        pq.add(new DNode(start.getId(), 0.0));

        while (!pq.isEmpty()) {
            DNode curr = pq.poll();

            // 이미 더 짧은 경로를 찾았다면 패스
            if (curr.w > dists.getOrDefault(curr.id, Double.MAX_VALUE)) continue;

            // 목적지 허브에 도착했다면 탐색 중단
            if (curr.id.equals(end.getId())) break;

            HubResponse u = getH(allHubs, curr.id);
            if (u == null) continue;

            for (HubResponse v : allHubs) {
                if (u.getId().equals(v.getId())) continue;

                double d = haversine(u, v);

                // 허브 간 거리가 너무 멀면 연결하지 않음
                if (d > 250) continue;

                // 기존 기록된 거리보다 현재 노드를 거쳐가는 거리가 더 짧으면 갱신
                double newDist = dists.get(u.getId()) + d;
                if (newDist < dists.getOrDefault(v.getId(), Double.MAX_VALUE)) {
                    dists.put(v.getId(), newDist);
                    prevs.put(v.getId(), u.getId());
                    pq.add(new DNode(v.getId(), newDist));
                }
            }
        }

        LinkedList<HubResponse> res = new LinkedList<>();
        UUID currentId = end.getId();

        if (dists.get(currentId) != null && dists.get(currentId) != Double.MAX_VALUE) {
            while (currentId != null) {
                HubResponse foundHub = getH(allHubs, currentId);
                if (foundHub == null) break;
                res.addFirst(foundHub);
                currentId = prevs.get(currentId);
            }
        }

        // 경로가 비었거나 시작점이 일치하지 않으면 연결 실패로 간주하고 출발/도착 직행 반환
        return res.isEmpty() || !res.getFirst().getId().equals(start.getId()) ? List.of(start, end) : res;
    }

    // 두 지점 간의 위도/경도를 이용한 직선 거리 계산
    private double haversine(HubResponse h1, HubResponse h2) {
        double lat1 = h1.getLatitude().doubleValue(), lon1 = h1.getLongitude().doubleValue();
        double lat2 = h2.getLatitude().doubleValue(), lon2 = h2.getLongitude().doubleValue();

        // 각각의 위도와 경도의 차이를 구한뒤 라디안 단위로 변환
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);

        // 위도는 어디서나 간격이 일정하여 1도 차이는 지구 전체 둘레 40,000/360으로 구하면 되지만
        // 경도는 위치에 따라서 폭의 차이가 있으므로 보정이 필요한데 cos(위도)값을 넣어서 계산하는 방식이다
        // 그래서 1라디안은 대략 0.0174이며 이를 각 위도 만큼에 차이만큰 곱하여 이를 2로 나누어 세로(위도)의 값을 구하고,
        // 경도는 위에 방식과 똑같지만 추가로 위도에 차에 따른 값 보정을 해줘야함으로 각각의 위도를 cos(위도 값)을 넣어서 구한후
        // 출발cos(위도)값 * 도착cos(위도)값 * 0.0087(1라디안/2)*(출발위도-도착위도)를 곱해서 가로 값을 구한다
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // 6371은 지구 반지름값이고 이를 세로(위도차이)+가로(경도차이) 값a을 구한다음
        // 각도 c = 2  x atan2(sqrt{a}, \sqrt{1-a})로 계산하여
        // 최종 예산 거리 = 6371 x c를 구할 수 있다.
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // 리스트에서 특정 ID에 해당하는 허브 객체 찾기
    private HubResponse getH(List<HubResponse> l, UUID id) {
        if (id == null) return null;
        return l.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 다익스트라 우선순위 큐를 위한 내부 정적 클래스
    @AllArgsConstructor
    private static class DNode implements Comparable<DNode> {
        UUID id; double w;
        @Override public int compareTo(DNode o) { return Double.compare(this.w, o.w); } // 거리가 짧은 순으로 정렬되도록 설정
    }
}