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
        @Schema(description = "허브 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID hub_id,

        @Schema(description = "허브 이름", example = "경기 남부 허브")
        String hub_name,

        @Schema(description = "허브 주소", example = "경기도 이천시 부발읍 경충대로 2091")
        String address,

        @Schema(description = "위도", example = "37.282600")
        BigDecimal latitude,

        @Schema(description = "경도", example = "127.442200")
        BigDecimal longitude,

        @Schema(description = "생성자", example = "master")
        String createdBy,

        @Schema(description = "생성 시각", example = "2026-04-07T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "수정자", example = "master", nullable = true)
        String updatedBy,

        @Schema(description = "수정 시각", example = "2026-04-07T11:00:00", nullable = true)
        LocalDateTime updatedAt,

        @Schema(description = "삭제자", example = "master", nullable = true)
        String deletedBy,

        @Schema(description = "삭제 시각", example = "2026-04-07T11:30:00", nullable = true)
        LocalDateTime deletedAt
)
{
    public static HubResponse from(Hub hub) {
        return new HubResponse(
                hub.getHubId(),
                hub.getHubName(),
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
                hub.getHubName(),
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