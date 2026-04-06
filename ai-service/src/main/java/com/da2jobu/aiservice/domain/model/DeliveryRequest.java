package com.da2jobu.aiservice.domain.model;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_delivery_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DeliveryRequest extends BaseEntity {

    @Id
    private UUID deliveryId;
    private String deliveryManagerSlackId;
    private String hubManagerSlackId;
    private String departureHubName;
    private String arrivalHubName;
    private String productName;
    private double departureLat;
    private double departureLon;
    private double arrivalLat;
    private double arrivalLon;
    private LocalDateTime scheduledDepartureTime;

    @Builder
    private DeliveryRequest(UUID deliveryId, String deliveryManagerSlackId, String hubManagerSlackId,
                            String departureHubName, String arrivalHubName,
                            String productName,
                            double departureLat, double departureLon,
                            double arrivalLat, double arrivalLon,
                            LocalDateTime scheduledDepartureTime) {
        this.deliveryId = deliveryId;
        this.deliveryManagerSlackId = deliveryManagerSlackId;
        this.hubManagerSlackId = hubManagerSlackId;
        this.departureHubName = departureHubName;
        this.arrivalHubName = arrivalHubName;
        this.productName = productName;
        this.departureLat = departureLat;
        this.departureLon = departureLon;
        this.arrivalLat = arrivalLat;
        this.arrivalLon = arrivalLon;
        this.scheduledDepartureTime = scheduledDepartureTime;
    }
}
