package com.delivery.hubpath.interfaces.dto.request;

import java.util.UUID;

public record UpdateHubPathRequest(
        UUID departHubId,
        UUID arriveHubId
)
{}