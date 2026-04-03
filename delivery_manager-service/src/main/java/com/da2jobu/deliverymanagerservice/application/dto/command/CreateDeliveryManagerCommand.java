package com.da2jobu.deliverymanagerservice.application.dto.command;

import com.da2jobu.deliverymanagerservice.domain.model.vo.DeliveryManagerType;

import java.util.UUID;

public record CreateDeliveryManagerCommand(
        UUID userId,
        UUID hubId,
        DeliveryManagerType type,
        UUID requesterId,
        String requesterRole
) {
}
