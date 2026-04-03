package com.da2jobu.deliveryservice.application.deliveryManager.dto.command;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;

import java.util.UUID;

public record CreateDeliveryManagerCommand(
        UUID userId,
        UUID hubId,
        DeliveryManagerType type,
        UUID requesterId,
        String requesterRole
) {
}
