package com.delivery.hub.application.dto;

import com.delivery.hub.domain.model.Hub;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "허브 생성 커맨드")
public record CreateHubCommand(
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public Hub toEntity() {
        return Hub.builder()
                .name(this.name)
                .address(this.address)
                .latitude(BigDecimal.valueOf(this.latitude))
                .longitude(BigDecimal.valueOf(this.longitude))
                .createdAt(LocalDateTime.now())
                .createdBy("master")
                .build();
    }
}