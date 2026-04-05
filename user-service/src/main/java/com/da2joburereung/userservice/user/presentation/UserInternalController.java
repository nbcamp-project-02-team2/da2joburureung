package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.user.application.UserApplicationService;
import com.da2joburereung.userservice.user.dto.response.InternalUserByIdResponseDto;
import com.da2joburereung.userservice.user.dto.response.InternalUserResponse;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class UserInternalController {

    private final UserApplicationService userApplicationService;

    @GetMapping("/by-username")
    public ResponseEntity<CommonResponse<InternalUserResponse>> findByUsername(
            @RequestParam("username") String username
    ) {
       InternalUserResponse response = userApplicationService.getUserByUsername(username);
       return CommonResponse.ok("사용자 내부 조회에 성공했습니다.", response);
    }

    @GetMapping("/by-userId/{userId}")
    public ResponseEntity<CommonResponse<InternalUserByIdResponseDto>> findByUserId(
            @PathVariable("userId")UUID userId
            ){
        InternalUserByIdResponseDto response = userApplicationService.getUserByUserId(userId);
        return CommonResponse.ok("사용자 내부 조회에 성공했습니다", response);
    }
}
