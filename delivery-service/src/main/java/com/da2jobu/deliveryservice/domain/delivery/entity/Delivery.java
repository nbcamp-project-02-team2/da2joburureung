package com.da2jobu.deliveryservice.domain.delivery.entity;

import com.da2jobu.deliveryservice.domain.delivery.vo.DeliveryStatus;
import common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "배송 엔티티")
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false, updatable = false)
    @Schema(description = "배송 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false)
    @Schema(description = "주문 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Schema(description = "배송 상태", example = "HUB_WAITING")
    private DeliveryStatus status;

    @Column(name = "origin_hub_id", nullable = false)
    @Schema(description = "출발 허브 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID originHubId;

    @Column(name = "destination_hub_id", nullable = false)
    @Schema(description = "도착 허브 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID destinationHubId;

    @Column(name = "delivery_address", nullable = false, length = 255)
    @Schema(description = "배송지 주소", example = "서울특별시 강남구 테헤란로 123")
    private String deliveryAddress;

    @Column(name = "receiver_name", nullable = false, length = 100)
    @Schema(description = "수령인 이름", example = "홍길동")
    private String receiverName;

    @Column(name = "receiver_slack_id", nullable = false, length = 100)
    @Schema(description = "수령인 슬랙 ID", example = "U12345678")
    private String receiverSlackId;

    @Column(name = "supplier_company_id")
    private UUID supplierCompanyId;

    @Column(name = "receiver_company_id")
    private UUID receiverCompanyId;

    @Column(name = "company_delivery_manager_id")
    @Schema(description = "업체 배송 담당자 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
    private UUID companyDeliveryManagerId;

    @Column(name = "request_note", length = 500)
    @Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요.", nullable = true)
    private String requestNote;

    @Column(name = "expected_duration_total_min")
    @Schema(description = "예상 총 소요 시간(분)", example = "120", nullable = true)
    private Integer expectedDurationTotalMin;

    @Column(name = "desired_delivery_at")
    @Schema(description = "희망 배송 시각", example = "2026-04-06T18:30:00", nullable = true)
    private LocalDateTime desiredDeliveryAt;

    @Column(name = "started_at")
    @Schema(description = "배송 시작 시각", example = "2026-04-06T17:00:00", nullable = true)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    @Schema(description = "배송 완료 시각", example = "2026-04-06T18:10:00", nullable = true)
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
            UUID supplierCompanyId,
            UUID receiverCompanyId,
            UUID companyDeliveryManagerId,
            String requestNote,
            Integer expectedDurationTotalMin,
            LocalDateTime desiredDeliveryAt,
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
        this.supplierCompanyId = supplierCompanyId;
        this.receiverCompanyId = receiverCompanyId;
        this.companyDeliveryManagerId = companyDeliveryManagerId;
        this.requestNote = requestNote;
        this.expectedDurationTotalMin = expectedDurationTotalMin;
        this.desiredDeliveryAt = desiredDeliveryAt;
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

    public void updateManagerId(UUID companyDeliveryManagerId) {
        this.companyDeliveryManagerId = companyDeliveryManagerId;
    }

    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
    }

}
