package common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class KakaoAddressService {

    @Value("${kakao.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public KakaoAddressService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    private String getAuthHeader() {
        return "KakaoAK " + apiKey;
    }

    public GeoPoint getGeoPoint(String address) {
        try {
            Map response = webClient.get()
                    .uri("https://dapi.kakao.com/v2/local/search/address.json?query=" + address)
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");

            if (documents != null && !documents.isEmpty()) {
                Map<String, Object> addressInfo = documents.get(0);
                return new GeoPoint(
                        new BigDecimal(addressInfo.get("y").toString()),
                        new BigDecimal(addressInfo.get("x").toString())
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Kakao Local API 호출 실패: " + e.getMessage());
        }
        throw new IllegalArgumentException("좌표를 찾을 수 없는 주소: " + address);
    }

    public RouteSummary getRouteSummary(GeoPoint origin, GeoPoint destination, GeoPoint waypoint) {
        String originParam = origin.longitude() + "," + origin.latitude();
        String destParam = destination.longitude() + "," + destination.latitude();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl("https://apis-navi.kakaomobility.com/v1/directions")
                .queryParam("origin", originParam)
                .queryParam("destination", destParam)
                .queryParam("priority", "RECOMMEND");

        if (waypoint != null) {
            uriBuilder.queryParam("waypoints", waypoint.longitude() + "," + waypoint.latitude());
        }

        try {
            Map response = webClient.get()
                    .uri(uriBuilder.build().toUri())
                    .header("Authorization", getAuthHeader())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (routes == null || routes.isEmpty()) {
                throw new RuntimeException("경로를 찾을 수 없습니다.");
            }

            Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary");

            return new RouteSummary(
                    Integer.parseInt(summary.get("distance").toString()),
                    Integer.parseInt(summary.get("duration").toString())
            );
        } catch (Exception e) {
            throw new RuntimeException("Kakao Mobility API 호출 실패: " + e.getMessage());
        }
    }

    public String generateMapUrl(String departName, GeoPoint origin, String arriveName, GeoPoint destination, String middleName, GeoPoint waypoint) {
        if (waypoint != null) {
            return String.format("https://map.kakao.com/link/by/car/%s,%f,%f/%s,%f,%f/%s,%f,%f",
                    departName, origin.latitude(), origin.longitude(),
                    middleName, waypoint.latitude(), waypoint.longitude(),
                    arriveName, destination.latitude(), destination.longitude());
        }
        return String.format("https://map.kakao.com/link/from/%s,%f,%f/to/%s,%f,%f",
                departName, origin.latitude(), origin.longitude(),
                arriveName, destination.latitude(), destination.longitude());
    }

    public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {}
    public record RouteSummary(int distanceMeter, int durationSecond) {}
}