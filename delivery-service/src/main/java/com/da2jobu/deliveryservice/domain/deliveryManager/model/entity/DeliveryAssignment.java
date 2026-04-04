package com.da2jobu.deliveryservice.domain.deliveryManager.model.entity;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryRouteRecordId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import common.entity.BaseEntity;
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
public class DeliveryAssignment extends BaseEntity {

    @EmbeddedId
    @AttributeOverride(name = "deliveryAssignmentId", column = @Column(name = "delivery_assignment_id"))
    private DeliveryAssignmentId deliveryAssignmentId;

    @Embedded
    @AttributeOverride(name = "deliveryManagerId", column = @Column(name = "delivery_manager_id", nullable = false))
    private DeliveryManagerId deliveryManagerId;

    @Embedded
    @AttributeOverride(name = "deliveryId", column = @Column(name = "delivery_id", nullable = false))
    private DeliveryId deliveryId;

    @Embedded
    @AttributeOverride(name = "deliveryRouteRecordId", column = @Column(name = "delivery_route_record_id"))
    private DeliveryRouteRecordId deliveryRouteRecordId;

    @Embedded
    @AttributeOverride(name = "hubId", column = @Column(name = "hub_id"))
    private HubId hubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
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

}