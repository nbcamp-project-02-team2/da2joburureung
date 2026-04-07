package com.da2jobu.aiservice.infrastructure.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WeatherClient {

    private final RestClient restClient;
    private final String apiKey;

    public WeatherClient(@Value("${weather.base-url}") String baseUrl,
                         @Value("${weather.api-key}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * 좌표 기반 현재 날씨 조회
     * @param lat 위도
     * @param lon 경도
     * @return 날씨 설명 텍스트 (예: "Rain, 15°C, 습도 80%")
     */
    public String getWeatherDescription(double lat, double lon) {
        try {
            WeatherResponse response = restClient.get()
                    .uri("/weather?lat={lat}&lon={lon}&appid={key}&units=metric&lang=kr",
                            lat, lon, apiKey)
                    .retrieve()
                    .body(WeatherResponse.class);

            if (response == null || response.weather() == null || response.weather().isEmpty()) {
                return "날씨 정보 없음";
            }

            String desc = response.weather().get(0).description();
            double temp = response.main().temp();
            int humidity = response.main().humidity();
            double windSpeed = response.wind().speed();

            return "%s, 기온 %.1f°C, 습도 %d%%, 풍속 %.1fm/s".formatted(desc, temp, humidity, windSpeed);
        } catch (Exception e) {
            log.warn("날씨 조회 실패 - lat:{}, lon:{}, error:{}", lat, lon, e.getMessage());
            return "날씨 정보를 가져올 수 없습니다";
        }
    }
}
