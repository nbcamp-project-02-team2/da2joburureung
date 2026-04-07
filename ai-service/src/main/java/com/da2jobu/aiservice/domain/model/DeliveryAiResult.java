package com.da2jobu.aiservice.domain.model;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "p_delivery_ai_result", schema = "ai_output")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryAiResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id")
    private UUID resultId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "estimated_arrival_time", nullable = false)
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "route_summary", nullable = false, columnDefinition = "TEXT")
    private String routeSummary;

    @Column(name = "weather_safety_comment", nullable = false, columnDefinition = "TEXT")
    private String weatherSafetyComment;

    /**
     * pgvector 임베딩 (text-embedding-3-small = 1536차원)
     * "[0.1, 0.2, ...]" 형태의 String으로 저장
     * PostgreSQL의 CAST(... AS vector) 쿼리와 호환
     */
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private String embedding;

    @Builder
    private DeliveryAiResult(UUID deliveryId, LocalDateTime estimatedArrivalTime,
                              String routeSummary, String weatherSafetyComment, float[] embedding) {
        this.deliveryId = deliveryId;
        this.estimatedArrivalTime = estimatedArrivalTime;
        this.routeSummary = routeSummary;
        this.weatherSafetyComment = weatherSafetyComment;
        this.embedding = embedding != null ? Arrays.toString(embedding) : null;
    }
}
