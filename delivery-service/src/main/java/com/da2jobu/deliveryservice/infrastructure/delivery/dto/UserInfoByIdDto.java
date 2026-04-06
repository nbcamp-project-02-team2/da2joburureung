package com.da2jobu.deliveryservice.infrastructure.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfoByIdDto(
        UUID userId,
        String username,
        String name,
        String email,
        String slackId,
        String userRole,
        UUID hubId
) {
}
