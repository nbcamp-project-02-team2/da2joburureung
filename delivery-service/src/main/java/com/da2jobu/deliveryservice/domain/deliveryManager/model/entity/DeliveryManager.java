package com.da2jobu.deliveryservice.domain.deliveryManager.model.entity;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.HubId;
import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.UserId;
import common.entity.BaseEntity;
import common.exception.CustomException;
import common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "p_delivery_manager",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_delivery_manager_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uq_delivery_manager_type_hub_seq", columnNames = {"type", "hub_id", "seq"})
        })
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

    public void update(HubId hubId, DeliveryManagerType type, Integer seq) {
        this.hubId = hubId;
        this.type = type;
        this.seq = seq;
    }

    public static boolean isHubDeliveryManager(DeliveryManagerType type, HubId hubId) {
        if (DeliveryManagerType.HUB_DELIVERY == type) {
            if (hubId == null){
                return true;
            } else {
                throw new CustomException(ErrorCode.DELIVERY_MANAGER_HUB_NOT_ALLOWED);
            }

        } else {
            if (hubId != null){
                return false;
            } else {
                throw new CustomException(ErrorCode.DELIVERY_MANAGER_HUB_REQUIRED);
            }
        }
    }

    /**
     * 허브 변경 여부 확인
     */
    public boolean isHubChanged(UUID newHubId) {
        if (this.hubId == null) {
            return newHubId != null;
        }
        return !this.hubId.isSameAs(newHubId);
    }

    /**
     * 타입 변경 여부 확인
     */
    public boolean isTypeChanged(DeliveryManagerType newType) { return this.type != newType; }
}