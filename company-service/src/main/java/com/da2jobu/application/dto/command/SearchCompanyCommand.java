package com.da2jobu.application.dto.command;

import com.da2jobu.domain.model.vo.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(description = "업체 검색 커맨드")
public record SearchCompanyCommand(
        @Schema(description = "업체명 검색어", example = "서울", nullable = true)
        String name,

        @Schema(description = "업체 타입", example = "PRODUCER", nullable = true)
        CompanyType type,

        @Schema(description = "허브 ID", example = "11111111-1111-1111-1111-111111111111", nullable = true)
        UUID hubId,

        @Schema(description = "페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
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
