package com.da2joburereung.userservice.user.presentation;

import com.da2joburereung.userservice.user.application.AuthApplicationService;
import com.da2joburereung.userservice.user.dto.request.LoginRequest;
import com.da2joburereung.userservice.user.dto.request.SignupRequest;
import com.da2joburereung.userservice.user.dto.response.LoginResponse;
import com.da2joburereung.userservice.user.dto.response.SignupResponse;
import common.dto.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authApplicationService.signup(request);
        return CommonResponse.created("회원가입 요청이 완료되었습니다.", response);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authApplicationService.login(request);
        return CommonResponse.ok("로그인에 성공했습니다.", response);
    }
}