package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.user.application.UserApplicationService;
import com.da2joburereung.userservice.user.dto.response.InternalUserByIdResponseDto;
import com.da2joburereung.userservice.user.dto.response.InternalUserResponse;
import common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Internal User", description = "내부 서비스 간 사용자 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class UserInternalController {

    private final UserApplicationService userApplicationService;

    @Operation(summary = "username으로 사용자 내부 조회", description = "다른 서비스에서 username 기준으로 사용자 정보를 조회합니다.")
    @GetMapping("/by-username")
    public ResponseEntity<CommonResponse<InternalUserResponse>> findByUsername(
            @Parameter(description = "사용자 아이디(username)", example = "master01")
            @RequestParam("username") String username
    ) {
       InternalUserResponse response = userApplicationService.getUserByUsername(username);
       return CommonResponse.ok("사용자 내부 조회에 성공했습니다.", response);
    }

    @Operation(summary = "userId로 사용자 내부 조회", description = "다른 서비스에서 userId 기준으로 사용자 정보를 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<InternalUserByIdResponseDto>> findByUserId(
            @Parameter(
                    description = "조회할 사용자 ID",
                    example = "11111111-1111-1111-1111-111111111111"
            )
            @PathVariable UUID userId
    ) {
        InternalUserByIdResponseDto response = userApplicationService.getUserByUserId(userId);
        return CommonResponse.ok("사용자 내부 조회에 성공했습니다.", response);
    }
}
