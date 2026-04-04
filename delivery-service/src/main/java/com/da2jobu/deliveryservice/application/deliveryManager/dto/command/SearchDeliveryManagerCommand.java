package com.da2jobu.deliveryservice.application.deliveryManager.dto.command;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryManagerType;

import java.util.Set;
import java.util.UUID;

public record SearchDeliveryManagerCommand(
        DeliveryManagerType type,
        UUID hubId,
        int page,
        int size,
        UUID requesterId,
        String requesterRole
) {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_SIZE = 10;

    public int validatedSize() {
        return ALLOWED_SIZES.contains(size) ? size : DEFAULT_SIZE;
    }

    public int validatedPage() {
        return Math.max(page, 0);
    }
}