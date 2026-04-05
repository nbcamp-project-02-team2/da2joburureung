package com.da2jobu.deliveryservice.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(
        List<T> content
) {
}