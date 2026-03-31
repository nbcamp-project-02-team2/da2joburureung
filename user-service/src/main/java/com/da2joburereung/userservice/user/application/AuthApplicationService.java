package com.da2joburereung.userservice.user.application;

import com.da2joburereung.userservice.global.security.JwtProvider;
import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRepository;
import com.da2joburereung.userservice.user.domain.UserStatus;
import com.da2joburereung.userservice.user.dto.request.LoginRequest;
import com.da2joburereung.userservice.user.dto.request.SignupRequest;
import com.da2joburereung.userservice.user.dto.response.LoginResponse;
import com.da2joburereung.userservice.user.dto.response.SignupResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthApplicationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createPendingUser(
                request.username(),
                encodedPassword,
                request.name(),
                request.slackId(),
                request.role(),
                request.hubId(),
                request.companyId()
        );

        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getStatus()
        );
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findActiveByUsername(request.username())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != UserStatus.APPROVED) {
            throw new CustomException(ErrorCode.USER_NOT_APPROVED);
        }

        String accessToken = jwtProvider.generateAccessToken(user);

        return new LoginResponse(
                accessToken,
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus()
        );
    }
}
