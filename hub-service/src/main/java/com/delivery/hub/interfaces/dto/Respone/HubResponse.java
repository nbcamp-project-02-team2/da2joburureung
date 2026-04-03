package com.delivery.hub.interfaces.dto.Respone;

import com.delivery.hub.domain.model.Hub;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "허브 정보 응답")
public record HubResponse(
        UUID hub_id,
        String hub_name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        String deletedBy,
        LocalDateTime deletedAt
)
{
    public static HubResponse from(Hub hub) {
        return new HubResponse(
                hub.getHubId(),
                hub.getHub_name(),
                hub.getAddress(),
                hub.getLatitude(),
                hub.getLongitude(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    public static HubResponse detailFrom(Hub hub) {
        return new HubResponse(
                hub.getHubId(),
                hub.getHub_name(),
                hub.getAddress(),
                hub.getLatitude(),
                hub.getLongitude(),
                hub.getCreatedBy(),
                hub.getCreatedAt(),
                hub.getUpdatedBy(),
                hub.getUpdatedAt(),
                hub.getDeletedBy(),
                hub.getDeletedAt()
        );
    }
}