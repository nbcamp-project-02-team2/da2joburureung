package com.delivery.hub.interfaces.dto.Respone;

import com.delivery.hub.domain.model.Hub;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "허브 정보 응답")
public record CreateHubResponse(
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
)
{
    public static CreateHubResponse from(Hub hub) {
        return new CreateHubResponse(
                hub.getHub_name(),
                hub.getAddress(),
                hub.getLatitude(),
                hub.getLongitude()
        );
    }
}