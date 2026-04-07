package com.da2joburereung.userservice.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


@Schema(description = "사용자 목록 페이징 응답")
public record UserPageResponse(

        @Schema(description = "사용자 목록")
        List<UserResponse> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 데이터 수", example = "25")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,

        @Schema(description = "첫 페이지 여부", example = "true")
        boolean first,

        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean last) {
}
