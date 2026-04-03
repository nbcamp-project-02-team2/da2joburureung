package com.da2jobu.deliverymanagerservice.infrastructure.client.dto;

import java.util.UUID;

public record HubResponse(
        UUID hubId,
        UUID managerId
) {
}