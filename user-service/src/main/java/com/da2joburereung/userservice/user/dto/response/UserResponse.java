package com.da2joburereung.userservice.user.dto.response;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 상세 응답")
public record UserResponse(

        @Schema(description = "사용자 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID userId,

        @Schema(description = "아이디", example = "master01")
        String username,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "슬랙 ID", example = "U12345678")
        String slackId,

        @Schema(description = "사용자 권한", example = "MASTER")
        UserRole role,

        @Schema(description = "사용자 상태", example = "APPROVED")
        UserStatus status,

        @Schema(description = "허브 ID", example = "22222222-2222-2222-2222-222222222222", nullable = true)
        UUID hubId,

        @Schema(description = "업체 ID", example = "33333333-3333-3333-3333-333333333333", nullable = true)
        UUID companyId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getSlackId(),
                user.getRole(),
                user.getStatus(),
                user.getHubId(),
                user.getCompanyId()
        );
    }
}