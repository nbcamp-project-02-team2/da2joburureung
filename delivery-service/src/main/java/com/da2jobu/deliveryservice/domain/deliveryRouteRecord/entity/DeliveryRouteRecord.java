package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_delivery_route_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "배송 경로 기록 엔티티")
public class DeliveryRouteRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_route_record_id", nullable = false, updatable = false)
    @Schema(description = "배송 경로 기록 ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID deliveryRouteRecordId;

    @Column(name = "delivery_id", nullable = false)
    @Schema(description = "배송 ID", example = "22222222-2222-2222-2222-222222222222")
    private UUID deliveryId;

    @Column(name = "sequence", nullable = false)
    @Schema(description = "배송 경로 순번", example = "1")
    private Integer sequence;

    @Column(name = "origin_id", nullable = false)
    @Schema(description = "출발지 ID", example = "33333333-3333-3333-3333-333333333333")
    private UUID originId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type", nullable = false, length = 20)
    @Schema(description = "출발지 타입", example = "HUB")
    private RouteLocationType originType;

    @Column(name = "origin_latitude")
    private Double originLatitude;

    @Column(name = "origin_longitude")
    private Double originLongitude;

    @Column(name = "destination_id", nullable = false)
    @Schema(description = "도착지 ID", example = "44444444-4444-4444-4444-444444444444")
    private UUID destinationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 20)
    @Schema(description = "도착지 타입", example = "COMPANY")
    private RouteLocationType destinationType;

    @Column(name = "destination_latitude")
    private Double destinationLatitude;

    @Column(name = "destination_longitude")
    private Double destinationLongitude;

    @Column(name = "expected_distance_km", nullable = false, precision = 10, scale = 2)
    @Schema(description = "예상 거리(km)", example = "12.50")
    private BigDecimal expectedDistanceKm;

    @Column(name = "expected_duration_min", nullable = false)
    @Schema(description = "예상 소요 시간(분)", example = "45")
    private Integer expectedDurationMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Schema(description = "배송 경로 상태", example = "HUB_WAITING")
    private DeliveryRouteStatus status;

    @Column(name = "delivery_manager_id")
    @Schema(description = "배송 담당자 ID", example = "55555555-5555-5555-5555-555555555555", nullable = true)
    private UUID deliveryManagerId;

    @Column(name = "real_distance_km", precision = 10, scale = 2)
    @Schema(description = "실제 이동 거리(km)", example = "10.80", nullable = true)
    private BigDecimal realDistanceKm;

    @Column(name = "real_duration_min")
    @Schema(description = "실제 소요 시간(분)", example = "40", nullable = true)
    private Integer realDurationMin;

    @Column(name = "remain_duration_min", nullable = false)
    @Schema(description = "남은 총 예상 시간(분)", example = "50")
    private Integer remainDurationMin;


    @Builder
    public DeliveryRouteRecord(
            UUID deliveryId,
            Integer sequence,
            UUID originId,
            RouteLocationType originType,
            Double originLatitude,
            Double originLongitude,
            UUID destinationId,
            RouteLocationType destinationType,
            Double destinationLatitude,
            Double destinationLongitude,
            BigDecimal expectedDistanceKm,
            Integer expectedDurationMin,
            DeliveryRouteStatus status,
            UUID deliveryManagerId,
            BigDecimal realDistanceKm,
            Integer realDurationMin,
            Integer remainDurationMin
    ) {
        this.deliveryId = deliveryId;
        this.sequence = sequence;
        this.originId = originId;
        this.originType = originType;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationId = destinationId;
        this.destinationType = destinationType;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.expectedDistanceKm = expectedDistanceKm;
        this.expectedDurationMin = expectedDurationMin;
        this.status = status;
        this.deliveryManagerId = deliveryManagerId;
        this.realDistanceKm = realDistanceKm;
        this.realDurationMin = realDurationMin;
        this.remainDurationMin = remainDurationMin;
    }

    public void updateStatus(DeliveryRouteStatus status) {
        this.status = status;
    }

    public void updateMetrics(BigDecimal realDistanceKm, Integer realDurationMin) {
        this.realDistanceKm = realDistanceKm;
        this.realDurationMin = realDurationMin;
    }

    public void updateManagerId(UUID deliveryManagerId) {
        this.deliveryManagerId = deliveryManagerId;
    }

    public void updateRemainDurationMin(Integer remainDurationMin) {
        this.remainDurationMin = remainDurationMin;
    }

    public void updateDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void updateCoordinates(
            Double originLatitude,
            Double originLongitude,
            Double destinationLatitude,
            Double destinationLongitude
    ) {
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
    }

    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
    }
}
