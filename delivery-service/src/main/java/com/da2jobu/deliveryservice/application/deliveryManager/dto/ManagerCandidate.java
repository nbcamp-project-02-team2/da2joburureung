package com.da2jobu.deliveryservice.application.deliveryManager.dto;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.entity.DeliveryManager;

import java.time.Duration;

public record ManagerCandidate(
        DeliveryManager manager,
        Duration idleDuration,
        int seq
) {
    public static ManagerCandidate create(DeliveryManager manager, Duration idleDuration, int seq) {
        return new ManagerCandidate(manager, idleDuration, seq);
    }
}
