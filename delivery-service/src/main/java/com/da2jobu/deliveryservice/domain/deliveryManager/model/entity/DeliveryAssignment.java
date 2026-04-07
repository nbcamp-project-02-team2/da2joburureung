package com.da2jobu.deliveryservice.domain.deliveryManager.model.entity;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.*;
import common.entity.BaseEntity;
import common.exception.CustomException;
import common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "p_delivery_assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "배송 담당자 배정 엔티티")
public class DeliveryAssignment extends BaseEntity {

    @EmbeddedId
    @AttributeOverride(name = "deliveryAssignmentId", column = @Column(name = "delivery_assignment_id"))
    @Schema(description = "배송 배정 ID")
    private DeliveryAssignmentId deliveryAssignmentId;

    @Embedded
    @AttributeOverride(name = "deliveryManagerId", column = @Column(name = "delivery_manager_id", nullable = false))
    @Schema(description = "배송 담당자 ID")
    private DeliveryManagerId deliveryManagerId;

    @Embedded
    @AttributeOverride(name = "deliveryId", column = @Column(name = "delivery_id", nullable = false))
    @Schema(description = "배송 ID")
    private DeliveryId deliveryId;

    @Embedded
    @AttributeOverride(name = "deliveryRouteRecordId", column = @Column(name = "delivery_route_record_id", nullable = false))
    @Schema(description = "배송 경로 기록 ID")
    private DeliveryRouteRecordId deliveryRouteRecordId;

    @Embedded
    @AttributeOverride(name = "hubId", column = @Column(name = "hub_id"))
    @Schema(description = "허브 ID")
    private HubId hubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Schema(description = "배정 상태", example = "ASSIGNED")
    private DeliveryAssignmentStatus status;

    // ── Factory Method ────────────────────────────────────────────────────────

    public static DeliveryAssignment create(
            DeliveryManagerId deliveryManagerId,
            DeliveryId deliveryId,
            DeliveryRouteRecordId deliveryRouteRecordId,
            HubId hubId
    ) {
        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.deliveryAssignmentId = DeliveryAssignmentId.of();
        assignment.deliveryManagerId = deliveryManagerId;
        assignment.deliveryId = deliveryId;
        assignment.deliveryRouteRecordId = deliveryRouteRecordId;
        assignment.hubId = hubId;
        assignment.status = DeliveryAssignmentStatus.ASSIGNED;
        return assignment;
    }

    public void complete() {
        if (this.status != DeliveryAssignmentStatus.ASSIGNED) {
            throw new CustomException(ErrorCode.INVALID_DELIVERY_STATUS);
        }
        this.status = DeliveryAssignmentStatus.COMPLETED;
    }
}