package com.da2joburereung.userservice.user.application;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRepository;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import com.da2joburereung.userservice.user.dto.response.InternalUserByIdResponseDto;
import com.da2joburereung.userservice.user.dto.response.InternalUserResponse;
import com.da2joburereung.userservice.user.dto.response.UserPageResponse;
import com.da2joburereung.userservice.user.dto.response.UserResponse;
import common.exception.CustomException;
import common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public InternalUserByIdResponseDto getUserByUserId(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new InternalUserByIdResponseDto(
                user.getUserId(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId()
        );
    }

    public UserPageResponse getUsers(String keyword, UserRole role, UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchActiveUsers(keyword, role, status, pageable);

        return new UserPageResponse(
                userPage.getContent().stream()
                        .map(UserResponse::from)
                        .toList(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast()
        );
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

    public InternalUserResponse getUserByUsername(String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return InternalUserResponse.from(user);
    }

    public InternalUserByIdResponseDto getUserByUserId(UUID userId) {
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new InternalUserByIdResponseDto(
                user.getUserId(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId()
        );
    }
}