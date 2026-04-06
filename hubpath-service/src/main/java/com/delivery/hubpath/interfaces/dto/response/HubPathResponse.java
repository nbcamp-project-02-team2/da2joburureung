package com.delivery.hubpath.interfaces.dto.response;

import com.delivery.hubpath.domain.model.HubPath;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HubPathResponse(
        UUID hub_path_id,
        UUID departHubId,
        String departHubName,
        UUID arriveHubId,
        String arriveHubName,
        List<StepDto> steps, // 모든 경유지 구간 정보를 담는 리스트
        BigDecimal totalDistance,
        Integer totalDuration,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt,
        String deletedBy,
        LocalDateTime deletedAt
) {
    // 구간별 상세 정보를 담을 내부 DTO
    public record StepDto(
            int stepOrder,
            UUID startHubId,
            String startHubName,
            UUID endHubId,
            String endHubName,
            BigDecimal distance,
            Integer duration
    ) {}

    public static HubPathResponse from(HubPath entity) {
        return new HubPathResponse(
                entity.getId(),
                entity.getDepartHubId(),
                entity.getDepartHubName(),
                entity.getArriveHubId(),
                entity.getArriveHubName(),
                null,
                entity.getTotalDistance(),
                entity.getTotalDuration(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getDeletedBy(),
                entity.getDeletedAt()
        );
    }

    public static HubPathResponse detailFrom(HubPath entity) {
        List<StepDto> stepDtos = null;

        // PathSteps 리스트의 모든 구간을 순서대로 변환
        if (entity.getPathSteps() != null && !entity.getPathSteps().isEmpty()) {
            stepDtos = entity.getPathSteps().stream()
                    .map(step -> new StepDto(
                            step.getStepOrder(),
                            step.getStartHubId(),
                            step.getStartHubName(),
                            step.getEndHubId(),
                            step.getEndHubName(),
                            step.getDistance(),
                            step.getDuration()
                    ))
                    .collect(Collectors.toList());
        }

        return new HubPathResponse(
                entity.getId(),
                entity.getDepartHubId(),
                entity.getDepartHubName(),
                entity.getArriveHubId(),
                entity.getArriveHubName(),
                stepDtos,
                entity.getTotalDistance(),
                entity.getTotalDuration(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt(),
                entity.getDeletedBy(),
                entity.getDeletedAt()
        );
    }
}