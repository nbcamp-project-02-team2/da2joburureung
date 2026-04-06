package com.delivery.hubpath.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_hub_path")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class HubPath extends common.entity.BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "hub_path_id")
    private UUID id;

    private UUID departHubId;
    private String departHubName;

    private UUID arriveHubId;
    private String arriveHubName;

    private BigDecimal totalDistance;
    private Integer totalDuration;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_path_id")
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<HubPathStep> pathSteps = new ArrayList<>();

    public void addStep(HubPathStep step) {
        this.pathSteps.add(step);
    }

    public void updateRouteInfo(UUID departHubId, String departHubName, UUID arriveHubId, String arriveHubName) {
        this.departHubId = departHubId;
        this.departHubName = departHubName;
        this.arriveHubId = arriveHubId;
        this.arriveHubName = arriveHubName;
    }

    public void updateTotalInfo(BigDecimal totalDistance, Integer totalDuration) {
        this.totalDistance = totalDistance;
        this.totalDuration = totalDuration;
    }

    public void setUpdatedBy(String username) {
        super.update(username);
    }
}