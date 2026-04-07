package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "내부 사용자 조회 응답")
public record InternalUserResponse(

        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "아이디", example = "master01")
        String username,

        @Schema(description = "이름", example = "홍길동")
        String name,
        String email,
        @Schema(description = "슬랙 ID", example = "U12345678")
        String slackId
) {
    public static InternalUserResponse from(User user) {
        return new InternalUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getSlackId()
        );
    }
}
