package com.da2jobu.infrastructure.messaging.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "p_outbox_event",
        indexes = @Index(name = "idx_outbox_status_created_at", columnList = "status, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public static OutboxEvent create(String aggregateType, UUID aggregateId,
                                     String eventType, String topic, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.eventId = UUID.randomUUID();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.topic = topic;
        event.payload = payload;
        event.status = OutboxStatus.PENDING;
        event.retryCount = 0;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    // 발행
    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    // 재시도 횟수
    public void incrementRetry(int maxRetry) {
        this.retryCount++;
        if (this.retryCount >= maxRetry) {
            this.status = OutboxStatus.FAILED;
        } else {
            this.status = OutboxStatus.PENDING;
        }
    }
}