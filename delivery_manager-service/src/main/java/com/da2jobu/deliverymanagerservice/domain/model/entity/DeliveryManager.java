package com.da2jobu.deliverymanagerservice.domain.model.entity;

import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;
import com.da2jobu.deliverymanagerservice.domain.model.vo.HubId;
import com.da2jobu.deliverymanagerservice.domain.model.vo.UserId;
import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "delivery_manager",
        uniqueConstraints = @UniqueConstraint(name = "uq_delivery_manager_user_id", columnNames = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DeliveryManager extends BaseEntity {

    @EmbeddedId
    @AttributeOverride(name = "deliveryManagerId", column = @Column(name = "delivery_manager_id"))
    private DeliveryManagerId deliveryManagerId;

    @Embedded
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "hubId", column = @Column(name = "hub_id"))
    private HubId hubId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DeliveryManagerType type;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    // ── Factory Method ────────────────────────────────────────────────────────

    public static DeliveryManager create(
            UserId userId,
            HubId hubId,
            DeliveryManagerType type,
            Integer seq
    ) {
        DeliveryManager manager = new DeliveryManager();
        manager.deliveryManagerId = DeliveryManagerId.of();
        manager.userId = userId;
        manager.hubId = hubId;
        manager.type = type;
        manager.seq = seq;
        return manager;
    }

    public static boolean isHubDeliveryManager(HubId hubId) {
        return hubId == null;
    }

}