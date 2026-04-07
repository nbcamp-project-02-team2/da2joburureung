package com.da2jobu.aiservice.infrastructure.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(
        List<WeatherDesc> weather,
        Main main,
        Wind wind
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDesc(String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp, int humidity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed) {}
}
