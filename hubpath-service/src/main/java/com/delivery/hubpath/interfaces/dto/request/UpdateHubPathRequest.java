package com.delivery.hubpath.interfaces.dto.request;

import java.util.UUID;

public record UpdateHubPathRequest(
        UUID hub_path_id,
        String departHubName,
        String arriveHubName

)
{}