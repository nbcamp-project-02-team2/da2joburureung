package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "공통 페이지 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지 데이터 목록")
        List<T> content
) {
}