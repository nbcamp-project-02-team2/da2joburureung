package com.delivery.hubpath.interfaces.dto.request;

public record SearchHubPathRequest(
        String depart_hub_name,
        String arrive_hub_name
) {}
