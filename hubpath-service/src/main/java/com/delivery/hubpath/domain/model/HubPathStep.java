package com.delivery.hubpath.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_hub_path_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HubPathStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "step_id")
    private UUID id;

    private Integer stepOrder;

    private UUID startHubId;
    private String startHubName;

    private UUID endHubId;
    private String endHubName;

    private BigDecimal distance;
    private Integer duration;
}
