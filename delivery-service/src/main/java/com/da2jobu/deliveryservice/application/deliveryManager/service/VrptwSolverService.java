package com.da2jobu.deliveryservice.application.deliveryManager.service;

import com.da2jobu.deliveryservice.application.deliveryManager.dto.CompanyDeliveryPoint;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VehicleRoute;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwInput;
import com.da2jobu.deliveryservice.application.deliveryManager.dto.VrptwResult;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Google OR-Tools 기반 VRPTW 솔버.
 * 클러스터링 + 경로 최적화 + 시간 윈도우를 단일 솔버로 통합
 */
@Slf4j
@Component
public class VrptwSolverService {

    static {
        Loader.loadNativeLibraries();
    }

    public VrptwResult solve(VrptwInput input) {
        List<CompanyDeliveryPoint> points = input.deliveryPoints();

        if (points.isEmpty()) {
            return new VrptwResult(List.of(), 0.0, true);
        }

        int nodeCount = points.size() + 1; // +1 for depot (허브)
        int vehicleCount = input.vehicleCount();
        int depot = 0;

        // 1. 매트릭스 구성 (DeliveryRouteRecord 데이터 활용)
        long[][] timeMatrix = buildTimeMatrix(points);
        long[][] distanceMatrix = buildDistanceMatrix(points);

        // 2. OR-Tools 모델 생성
        RoutingIndexManager manager =
                new RoutingIndexManager(nodeCount, vehicleCount, depot);
        RoutingModel routing = new RoutingModel(manager);

        // 3. 거리 콜백 등록
        int distanceCallback = routing.registerTransitCallback((long from, long to) -> {
            int fromNode = manager.indexToNode(from);
            int toNode = manager.indexToNode(to);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(distanceCallback);

        // 4. 시간 차원 추가
        int timeCallback = routing.registerTransitCallback((long from, long to) -> {
            int fromNode = manager.indexToNode(from);
            int toNode = manager.indexToNode(to);
            return timeMatrix[fromNode][toNode];
        });
        routing.addDimension(
                timeCallback,
                30,                              // 허용 대기 시간 (분)
                input.maxRouteTimeMinutes(),     // 매니저당 최대 시간 (480분)
                false,
                "Time"
        );

        // 5. 시간 윈도우 제약 설정
        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        for (int i = 0; i < points.size(); i++) {
            long index = manager.nodeToIndex(i + 1);
            long[] window = toTimeWindow(
                    points.get(i).requestedDeliveryTime(),
                    input.batchStartTime()
            );
            timeDimension.cumulVar(index).setRange(window[0], window[1]);
        }

        // 6. 풀기
        RoutingSearchParameters params = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(Duration.newBuilder().setSeconds(5).build())
                .build();

        Assignment solution = routing.solveWithParameters(params);

        if (solution == null) {
            log.warn("OR-Tools VRPTW 해를 찾지 못함 - nodeCount={}, vehicleCount={}",
                    nodeCount, vehicleCount);
            return new VrptwResult(List.of(), 0.0, false);
        }

        // 7. 결과 파싱
        return parseSolution(routing, manager, solution, points);
    }

    /**
     * Haversine 기반 시간 매트릭스.
     * - 허브↔업체: DB expectedDurationMin 유지 (도로 기반 실측이 직선 거리보다 정확)
     * - 업체↔업체: haversine(좌표A, 좌표B) → 평균 속도(40km/h)로 시간 변환
     */
    private long[][] buildTimeMatrix(List<CompanyDeliveryPoint> points) {
        int size = points.size() + 1;
        long[][] matrix = new long[size][size];

        for (int i = 0; i < points.size(); i++) {
            long duration = points.get(i).durationFromHubMin();
            matrix[0][i + 1] = duration;
            matrix[i + 1][0] = duration;
        }

        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double distKm = haversineKm(points.get(i), points.get(j));
                long timeMin = Math.round(distKm / AVERAGE_SPEED_KMH * 60);
                matrix[i + 1][j + 1] = timeMin;
                matrix[j + 1][i + 1] = timeMin;
            }
        }

        return matrix;
    }

    /**
     * Haversine 기반 거리 매트릭스.
     * - 허브↔업체: DB expectedDistanceKm (×100 정수화) 유지
     * - 업체↔업체: haversine 직선 거리 (×100 정수화)
     */
    private long[][] buildDistanceMatrix(List<CompanyDeliveryPoint> points) {
        int size = points.size() + 1;
        long[][] matrix = new long[size][size];

        for (int i = 0; i < points.size(); i++) {
            long distance = points.get(i).distanceFromHubKm()
                    .multiply(java.math.BigDecimal.valueOf(100)).longValue();
            matrix[0][i + 1] = distance;
            matrix[i + 1][0] = distance;
        }

        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                long distScaled = Math.round(haversineKm(points.get(i), points.get(j)) * 100);
                matrix[i + 1][j + 1] = distScaled;
                matrix[j + 1][i + 1] = distScaled;
            }
        }

        return matrix;
    }

    private static final double AVERAGE_SPEED_KMH = 40.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Haversine 공식으로 두 업체 간 직선 거리(km) 계산
     */
    private double haversineKm(CompanyDeliveryPoint a, CompanyDeliveryPoint b) {
        double lat1 = Math.toRadians(a.latitude().doubleValue());
        double lat2 = Math.toRadians(b.latitude().doubleValue());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.longitude().doubleValue() - a.longitude().doubleValue());

        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    private long[] toTimeWindow(java.time.LocalDateTime requestedTime,
                                java.time.LocalDateTime batchStart) {
        if (requestedTime == null) {
            return new long[]{0, 480};
        }
        long deadlineMin = java.time.Duration.between(batchStart, requestedTime).toMinutes();
        long startMin = Math.max(0, deadlineMin - 30);
        return new long[]{startMin, deadlineMin};
    }

    private VrptwResult parseSolution(RoutingModel routing, RoutingIndexManager manager,
                                      Assignment solution, List<CompanyDeliveryPoint> points) {
        List<VehicleRoute> routes = new ArrayList<>();
        double totalDistance = 0.0;

        for (int v = 0; v < manager.getNumberOfVehicles(); v++) {
            List<CompanyDeliveryPoint> ordered = new ArrayList<>();
            double vehicleDistance = 0.0;
            long index = routing.start(v);

            while (!routing.isEnd(index)) {
                int node = manager.indexToNode(index);
                if (node > 0) {
                    ordered.add(points.get(node - 1));
                }
                long prevIndex = index;
                index = solution.value(routing.nextVar(index));
                vehicleDistance += routing.getArcCostForVehicle(prevIndex, index, v);
            }

            if (!ordered.isEmpty()) {
                // 거리를 다시 km로 변환 (×100 했으므로 ÷100)
                double distanceKm = vehicleDistance / 100.0;
                routes.add(new VehicleRoute(v, ordered, distanceKm));
                totalDistance += distanceKm;
            }
        }

        return new VrptwResult(routes, totalDistance, true);
    }
}
