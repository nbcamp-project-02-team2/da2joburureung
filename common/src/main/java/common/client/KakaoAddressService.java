package common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class KakaoAddressService {

    @Value("${kakao.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final WebClient naviClient;

    public KakaoAddressService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://dapi.kakao.com").build();
        this.naviClient = webClientBuilder.baseUrl("https://apis-navi.kakaomobility.com").build();
    }

    public GeoPoint getGeoPoint(String address) {

        String authHeader = "KakaoAK " + apiKey;

        try {
            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");

            if (documents != null && !documents.isEmpty()) {
                Map<String, Object> addressInfo = documents.get(0);

                // 카카오는 x가 경도(longitude), y가 위도(latitude)입니다.
                BigDecimal longitude = new BigDecimal(addressInfo.get("x").toString());
                BigDecimal latitude = new BigDecimal(addressInfo.get("y").toString());

                return new GeoPoint(latitude, longitude);
            }

        } catch (Exception e) {
            System.err.println("호출 실패: " + e.getMessage());
            throw e;
        }

        throw new IllegalArgumentException("해당 주소의 좌표를 찾을 수 없습니다: " + address);
    }

    public RouteSummary getRouteSummary(GeoPoint origin, GeoPoint destination, GeoPoint waypoint) {
        String authHeader = "KakaoAK " + apiKey;

        // 경유지가 있을 경우 파라미터 구성
        String waypointsStr = (waypoint != null)
                ? waypoint.longitude() + "," + waypoint.latitude()
                : null;

        try {
            Map response = naviClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/v1/directions")
                                .queryParam("origin", origin.longitude() + "," + origin.latitude())
                                .queryParam("destination", destination.longitude() + "," + destination.latitude());

                        if (waypointsStr != null) {
                            uriBuilder.queryParam("waypoints", waypointsStr);
                        }

                        return uriBuilder.build();
                    })
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 응답 데이터에서 거리와 시간 추출
            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            Map<String, Object> summary = (Map<String, Object>) ((Map<String, Object>) routes.get(0)).get("summary");

            int distance = (int) summary.get("distance"); // 미터 단위
            int duration = (int) summary.get("duration"); // 초 단위

            return new RouteSummary(distance, duration);

        } catch (Exception e) {
            System.err.println("길찾기 API 호출 실패: " + e.getMessage());
            return new RouteSummary(0, 0);
        }
    }

    public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {}
    public record RouteSummary(int distanceMeter, int durationSecond) {}
}