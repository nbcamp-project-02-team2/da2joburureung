package com.delivery.hubpath.interfaces.dto.response;

import com.delivery.hubpath.domain.model.HubPath;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubPathResponse(
        UUID hub_path_id,
        String departHubName,
        String arriveHubName,
        String middleHubName,
        BigDecimal distance,
        Integer duration,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        String deletedBy,
        LocalDateTime deletedAt
)
{
    public static HubPathResponse from(HubPath hubPath) {
        return new HubPathResponse(
                hubPath.getHub_path_id(),
                hubPath.getDepartHubName(),
                hubPath.getArriveHubName(),
                hubPath.getMiddleHubName(),
                hubPath.getDistance(),
                hubPath.getDuration(),
                hubPath.getCreatedBy(),
                hubPath.getCreatedAt(),
                hubPath.getUpdatedBy(),
                hubPath.getUpdatedAt(),
                hubPath.getDeletedBy(),
                hubPath.getDeletedAt()
        );
    }
}
