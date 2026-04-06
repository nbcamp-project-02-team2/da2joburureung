package com.delivery.hubpath.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.delivery.hubpath.domain.model.HubPath;
import com.delivery.hubpath.domain.model.HubPathStep;
import com.delivery.hubpath.domain.repository.HubPathRepository;
import com.delivery.hubpath.infrastructure.client.HubResponse;
import common.client.KakaoAddressService;
import common.client.KakaoAddressService.GeoPoint;
import common.client.KakaoAddressService.RouteSummary;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubPathService {

    private final HubPathRepository hubPathRepository;
    private final KakaoAddressService kakaoService;

    @Transactional
    public HubPath createAndSavePath(HubResponse depart, HubResponse arrive, List<HubResponse> allHubs) {
        GeoPoint origin = new GeoPoint(depart.getLatitude(), depart.getLongitude());
        GeoPoint destination = new GeoPoint(arrive.getLatitude(), arrive.getLongitude());

        RouteSummary summary = kakaoService.getRouteSummary(origin, destination, null);
        validateRouteSummary(origin, destination, summary);

        List<HubResponse> pathNodes = new ArrayList<>();

        if (summary.distanceMeter() > 200000) {
            pathNodes = findDijkstraPath(depart, arrive, allHubs);
            if (pathNodes.size() < 3) {
                HubResponse m = findForceMiddleHub(depart, arrive, allHubs);
                if (m != null) {
                    pathNodes = List.of(depart, m, arrive);
                }
            }
        }

        if (pathNodes.isEmpty()) {
            pathNodes = List.of(depart, arrive);
        }

        HubPath hubPath = (HubPath) HubPath.builder()
                .departHubId(depart.getId())
                .departHubName(depart.getHub_name())
                .arriveHubId(arrive.getId())
                .arriveHubName(arrive.getHub_name())
                .build();

        long totalDistanceMeter = 0;
        int totalDurationSecond = 0;

        for (int i = 0; i < pathNodes.size() - 1; i++) {
            HubResponse startNode = pathNodes.get(i);
            HubResponse endNode = pathNodes.get(i + 1);

            RouteSummary legSummary = kakaoService.getRouteSummary(
                    new GeoPoint(startNode.getLatitude(), startNode.getLongitude()),
                    new GeoPoint(endNode.getLatitude(), endNode.getLongitude()),
                    null
            );

            HubPathStep step = HubPathStep.builder()
                    .stepOrder(i + 1)
                    .startHubId(startNode.getId())
                    .startHubName(startNode.getHub_name())
                    .endHubId(endNode.getId())
                    .endHubName(endNode.getHub_name())
                    .distance(BigDecimal.valueOf(legSummary.distanceMeter()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP))
                    .duration((int) Math.round(legSummary.durationSecond() / 60.0))
                    .build();

            hubPath.addStep(step);
            totalDistanceMeter += legSummary.distanceMeter();
            totalDurationSecond += legSummary.durationSecond();
        }

        hubPath.updateTotalInfo(
                BigDecimal.valueOf(totalDistanceMeter).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP),
                (int) Math.round(totalDurationSecond / 60.0)
        );

        return hubPathRepository.save(hubPath);
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

                if (d > 180) continue;

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

    // 다익스트라가 실패하거나 직행을 선택했을 때, 물리적 중간 지점에 가장 가까운 허브를 찾는 보조 메서드
    private HubResponse findForceMiddleHub(HubResponse start, HubResponse end, List<HubResponse> allHubs) {

        HubResponse closest = null;
        double minTotalDist = Double.MAX_VALUE;

        for (HubResponse h : allHubs) {
            if (h.getId().equals(start.getId()) || h.getId().equals(end.getId())) continue;

            double d1 = haversine(start, h);
            double d2 = haversine(h, end);
            double total = d1 + d2;

            if (total < minTotalDist) {
                minTotalDist = total;
                closest = h;
            }
        }
        return closest;
    }

    private void validateRouteSummary(GeoPoint origin, GeoPoint destination, RouteSummary summary) {
        boolean isDifferentLocation = !origin.equals(destination);

        if (isDifferentLocation && (summary.distanceMeter() <= 0 || summary.durationSecond() <= 0)) {
            throw new IllegalStateException("카카오 경로 호출에 실패했거나 유효하지 않은 경로 응답입니다. (0km/0분)");
        }
    }

    @AllArgsConstructor
    private static class DNode implements Comparable<DNode> {
        UUID id;
        double w;

        @Override
        public int compareTo(DNode o) {
            return Double.compare(this.w, o.w); // 거리가 짧은 순으로 정렬되도록 설정
        }
    }
}