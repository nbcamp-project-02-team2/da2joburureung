package com.da2jobu.infrastructure.client.dto;

import java.util.UUID;

public record HubResponse(
        UUID hubId,
        String name,
        String address
) {}