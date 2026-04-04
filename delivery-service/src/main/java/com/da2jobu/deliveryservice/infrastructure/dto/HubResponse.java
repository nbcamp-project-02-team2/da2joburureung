package com.da2jobu.deliveryservice.infrastructure.dto;

import java.util.UUID;

public record HubResponse(
        UUID hubId,
        UUID managerId
) {
}