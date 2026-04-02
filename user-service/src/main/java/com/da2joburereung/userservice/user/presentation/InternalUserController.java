package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.user.application.UserApplicationService;
import com.da2joburereung.userservice.user.dto.response.InternalUserResponse;
import common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserApplicationService userApplicationService;

    @GetMapping("/by-username")
    public ResponseEntity<CommonResponse<InternalUserResponse>> findByUsername(
            @RequestParam("username") String username
    ) {
       InternalUserResponse response = userApplicationService.getUserByUsername(username);
       return CommonResponse.ok("사용자 내부 조회에 성공했습니다.", response);
    }
}
