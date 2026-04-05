package com.da2jobu.deliveryservice.domain.deliveryManager.model.vo;

import java.time.Duration;

public record ManagerIdleDuration(
        DeliveryManagerId managerId,
        Duration idleDuration           //경과시간
) {
}

