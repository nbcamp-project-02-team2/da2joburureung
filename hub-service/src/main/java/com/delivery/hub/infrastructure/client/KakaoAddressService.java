package com.delivery.hub.infrastructure.client;

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

    public KakaoAddressService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://dapi.kakao.com").build();
    }

    public GeoPoint getGeoPoint(String address) {

        String authHeader = "KakaoAK " + apiKey;

        try {
            // API 호출 및 Map으로 응답 받기
            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(Map.class) // String 대신 Map으로 받으면 파싱이 쉽습니다.
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

    public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {}
}