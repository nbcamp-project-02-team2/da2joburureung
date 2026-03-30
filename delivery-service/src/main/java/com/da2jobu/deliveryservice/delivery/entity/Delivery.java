package com.da2jobu.deliveryservice.delivery.entity;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DeliveryStatus status;

    @Column(name = "origin_hub_id", nullable = false)
    private UUID originHubId;

    @Column(name = "destination_hub_id", nullable = false)
    private UUID destinationHubId;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_slack_id", nullable = false, length = 100)
    private String receiverSlackId;

    @Column(name = "company_delivery_manager_id", nullable = false)
    private UUID companyDeliveryManagerId;

    @Column(name = "request_note", length = 500)
    private String requestNote;

    @Column(name = "expected_duration_total_min")
    private Integer expectedDurationTotalMin;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Delivery(
            UUID orderId,
            DeliveryStatus status,
            UUID originHubId,
            UUID destinationHubId,
            String deliveryAddress,
            String receiverName,
            String receiverSlackId,
            UUID companyDeliveryManagerId,
            String requestNote,
            Integer expectedDurationTotalMin,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        this.orderId = orderId;
        this.status = status;
        this.originHubId = originHubId;
        this.destinationHubId = destinationHubId;
        this.deliveryAddress = deliveryAddress;
        this.receiverName = receiverName;
        this.receiverSlackId = receiverSlackId;
        this.companyDeliveryManagerId = companyDeliveryManagerId;
        this.requestNote = requestNote;
        this.expectedDurationTotalMin = expectedDurationTotalMin;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public void updateStatus(DeliveryStatus status) {
        this.status = status;
    }

    public void markStarted(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void markCompleted(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
    }

}
