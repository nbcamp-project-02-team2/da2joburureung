package com.da2joburereung.userservice.user.application;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRepository;
import com.da2joburereung.userservice.user.dto.response.UserResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserResponse getUser(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public UserResponse getMyInfo(String userId) {
        User user = userRepository.findActiveById(UUID.fromString(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public void approveUser(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.approve();
    }

    @Transactional
    public void rejectUser(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.reject();
    }

    @Transactional
    public void deleteUser(UUID userId, String deletedBy) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.delete(deletedBy);
    }
}