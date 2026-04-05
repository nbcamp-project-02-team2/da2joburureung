package com.da2jobu.deliveryservice.application.deliveryManager.dto.command;

import com.da2jobu.deliveryservice.domain.deliveryManager.model.vo.DeliveryAssignmentStatus;

import java.util.Set;
import java.util.UUID;

public record SearchDeliveryAssignmentCommand(
        UUID deliveryManagerId,
        DeliveryAssignmentStatus status,
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

