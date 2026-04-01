package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.global.security.RoleAuthorizer;
import com.da2joburereung.userservice.user.application.UserApplicationService;
import com.da2joburereung.userservice.user.dto.response.UserResponse;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final RoleAuthorizer roleAuthorizer;

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> getMyInfo(
            @RequestHeader("X-User-Id") String userId
    ) {
        UserResponse response = userApplicationService.getMyInfo(userId);
        return CommonResponse.ok("내 정보 조회에 성공했습니다.", response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireMaster(role);
        UserResponse response = userApplicationService.getUser(userId);
        return CommonResponse.ok("사용자 조회에 성공했습니다.", response);
    }

    @PatchMapping("/{userId}/approvals")
    public ResponseEntity<CommonResponse<?>> approveUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireAnyOf(role, "MASTER", "HUB_MANAGER");

        log.info("approve request role={}", role);
        userApplicationService.approveUser(userId);
        return CommonResponse.ok("사용자 승인이 완료되었습니다.");
    }

    @PatchMapping("/{userId}/rejection")
    public ResponseEntity<CommonResponse<?>> rejectUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireAnyOf(role, "MASTER", "HUB_MANAGER");

        userApplicationService.rejectUser(userId);
        return CommonResponse.ok("사용자 거절이 완료되었습니다.");
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<?>> deleteUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String requestUserId
    ) {
        roleAuthorizer.requireMaster(role);

        userApplicationService.deleteUser(userId, requestUserId);
        return CommonResponse.ok("사용자 삭제가 완료되었습니다.");
    }
}