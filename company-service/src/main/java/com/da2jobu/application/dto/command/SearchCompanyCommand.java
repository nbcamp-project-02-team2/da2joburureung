package com.da2jobu.application.dto.command;

import com.da2jobu.domain.model.vo.CompanyType;

import java.util.Set;
import java.util.UUID;

public record SearchCompanyCommand(
        String name,
        CompanyType type,
        UUID hubId,
        int page,
        int size
) {
    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_SIZE = 10;

    public int validatedSize() {
        return ALLOWED_SIZES.contains(size) ? size : DEFAULT_SIZE;
    }

    public int validatedPage() {
        return Math.max(page, 0);  // 최소 0페이지
    }
}
