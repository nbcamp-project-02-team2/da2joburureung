package com.delivery.hubpath.interfaces.dto.response;

import com.delivery.hubpath.domain.model.HubPath;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubPathResponse(
        UUID hub_path_id,
        UUID departHubId,
        String departHubName,
        UUID arriveHubId,
        String arriveHubName,
        String middleHubId,
        String middleHubName,
        BigDecimal firstDistance,
        Integer firstDuration,
        BigDecimal secondDistance,
        Integer secondDuration,
        BigDecimal distance,
        Integer duration,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        String deletedBy,
        LocalDateTime deletedAt
) {
    public static HubPathResponse from(HubPath entity) {
        return new HubPathResponse(
                entity.getHub_path_id(),
                entity.getDepartHubId(),
                entity.getDepartHubName(),
                entity.getArriveHubId(),
                entity.getArriveHubName(),
                entity.getMiddleHubName(),
                entity.getMiddleHubName(),
                null, null, null, null,
                entity.getDistance(),
                entity.getDuration(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getDeletedBy(),
                entity.getDeletedAt()
        );
    }
    public static HubPathResponse detailFrom(HubPath entity) {
        return new HubPathResponse(
                entity.getHub_path_id(),
                entity.getDepartHubId(),
                entity.getDepartHubName(),
                entity.getArriveHubId(),
                entity.getArriveHubName(),
                entity.getMiddleHubId(),
                entity.getMiddleHubName(),
                entity.getFirstDistance(),
                entity.getFirstDuration(),
                entity.getSecondDistance(),
                entity.getSecondDuration(),
                entity.getDistance(),
                entity.getDuration(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getDeletedBy(),
                entity.getDeletedAt()
        );
    }
}