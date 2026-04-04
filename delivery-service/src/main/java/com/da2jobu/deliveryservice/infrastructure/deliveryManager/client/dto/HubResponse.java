package com.da2jobu.deliveryservice.infrastructure.deliveryManager.client.dto;

import java.util.UUID;

public record HubResponse(
        UUID hubId,
        UUID managerId
) {
}