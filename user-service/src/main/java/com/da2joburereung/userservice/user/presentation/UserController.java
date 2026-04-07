package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.global.security.RoleAuthorizer;
import com.da2joburereung.userservice.user.application.UserApplicationService;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import com.da2joburereung.userservice.user.dto.request.UserMeUpdateRequest;
import com.da2joburereung.userservice.user.dto.request.UserPasswordUpdateRequest;
import com.da2joburereung.userservice.user.dto.response.UserPageResponse;
import com.da2joburereung.userservice.user.dto.response.UserResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@Tag(name = "User", description = "사용자 조회/승인/삭제/수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final RoleAuthorizer roleAuthorizer;

    @Operation(summary = "내 정보 조회", description = "게이트웨이가 전달한 X-User-Id 헤더를 기준으로 내 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> getMyInfo(
            @RequestHeader("X-User-Id") String userId
    ) {
        UserResponse response = userApplicationService.getMyInfo(userId);
        return CommonResponse.ok("내 정보 조회에 성공했습니다.", response);
    }

    @Operation(summary = "내 정보 수정", description = "게이트웨이가 전달한 X-User-Id 헤더를 기준으로 내 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<CommonResponse<UserResponse>> updateMyInfo(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserMeUpdateRequest request
    ) {
        UserResponse response = userApplicationService.updateMyInfo(userId, request);
        return CommonResponse.ok("내 정보 수정에 성공했습니다.", response);
    }

    @Operation(summary = "내 비밀번호 변경", description = "게이트웨이가 전달한 X-User-Id 헤더를 기준으로 내 비밀번호를 변경합니다.")
    @PatchMapping("/me/password")
    public ResponseEntity<CommonResponse<?>> updateMyPassword(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserPasswordUpdateRequest request
    ) {
        userApplicationService.updateMyPassword(userId, request);
        return CommonResponse.ok("비밀번호 변경에 성공했습니다.");
    }

    @Operation(summary = "사용자 단건 조회", description = "MASTER 권한으로 특정 사용자를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserResponse>> getUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireMaster(role);
        UserResponse response = userApplicationService.getUser(userId);
        return CommonResponse.ok("사용자 조회에 성공했습니다.", response);
    }

    @Operation(summary = "사용자 승인", description = "MASTER 또는 HUB_MANAGER 권한으로 가입 대기 사용자를 승인합니다.")
    @PatchMapping("/{userId}/approvals")
    public ResponseEntity<CommonResponse<?>> approveUser(
            @PathVariable UUID userId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireAnyOf(role, "MASTER", "HUB_MANAGER");

        log.info("approve request role={}", role);
        userApplicationService.approveUser(userId);
        return CommonResponse.ok("사용자 승인이 완료되었습니다.");
    }

    @Operation(summary = "사용자 거절", description = "MASTER 또는 HUB_MANAGER 권한으로 가입 대기 사용자를 거절합니다.")
    @PatchMapping("/{userId}/rejection")
    public ResponseEntity<CommonResponse<?>> rejectUser(
            @PathVariable UUID userId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String role
    ) {
        roleAuthorizer.requireAnyOf(role, "MASTER", "HUB_MANAGER");

        userApplicationService.rejectUser(userId);
        return CommonResponse.ok("사용자 거절이 완료되었습니다.");
    }

    @Operation(summary = "사용자 삭제", description = "MASTER 권한으로 특정 사용자를 삭제합니다.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<CommonResponse<?>> deleteUser(
            @PathVariable UUID userId,
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String role,
            @Parameter(description = "요청 사용자 ID", example = "11111111-1111-1111-1111-111111111111")
            @RequestHeader("X-User-Id") String requestUserId
    ) {
        roleAuthorizer.requireMaster(role);

        userApplicationService.deleteUser(userId, requestUserId);
        return CommonResponse.ok("사용자 삭제가 완료되었습니다.");
    }

    @Operation(summary = "사용자 목록 조회", description = "조건에 따라 사용자 목록을 페이징 조회합니다. MASTER 권한이 필요합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<UserPageResponse>> getUsers(
            @Parameter(description = "요청 사용자 역할", example = "MASTER")
            @RequestHeader("X-User-Role") String requestRole,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
     roleAuthorizer.requireMaster(requestRole);

     UserPageResponse response = userApplicationService.getUsers(keyword, role, status, page, size);
     return CommonResponse.ok("사용자 목록 조회에 성공했습니다.", response);
    }
}