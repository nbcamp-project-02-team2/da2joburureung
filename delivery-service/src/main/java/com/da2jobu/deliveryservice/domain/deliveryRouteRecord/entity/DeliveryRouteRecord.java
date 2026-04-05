package com.da2jobu.deliveryservice.domain.deliveryRouteRecord.entity;

import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.DeliveryRouteStatus;
import com.da2jobu.deliveryservice.domain.deliveryRouteRecord.vo.RouteLocationType;
import common.entity.BaseEntity;
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
public class DeliveryRouteRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_route_record_id", nullable = false, updatable = false)
    private UUID deliveryRouteRecordId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "origin_id", nullable = false)
    private UUID originId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_type", nullable = false, length = 20)
    private RouteLocationType originType;

    @Column(name = "destination_id", nullable = false)
    private UUID destinationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 20)
    private RouteLocationType destinationType;

    @Column(name = "expected_distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal expectedDistanceKm;

    @Column(name = "expected_duration_min", nullable = false)
    private Integer expectedDurationMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryRouteStatus status;

    @Column(name = "delivery_manager_id")
    private UUID deliveryManagerId;

    @Column(name = "real_distance_km", precision = 10, scale = 2)
    private BigDecimal realDistanceKm;

    @Column(name = "real_duration_min")
    private Integer realDurationMin;

    @Column(name = "remain_duration_min", nullable = false)
    private Integer remainDurationMin;

    @Builder
    public DeliveryRouteRecord(
            UUID deliveryId,
            Integer sequence,
            UUID originId,
            RouteLocationType originType,
            UUID destinationId,
            RouteLocationType destinationType,
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
        this.destinationId = destinationId;
        this.destinationType = destinationType;
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

    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
    }
}
