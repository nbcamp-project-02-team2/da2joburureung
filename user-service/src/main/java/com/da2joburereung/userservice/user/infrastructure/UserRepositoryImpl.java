package com.da2joburereung.userservice.user.infrastructure;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRepository;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userJpaRepository.findById(userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findActiveById(UUID userId) {
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Override
    public Optional<User> findActiveByUsername(String username) {
        return userJpaRepository.findByUsernameAndDeletedAtIsNull(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public Page<User> searchActiveUsers(String keyword, UserRole role, UserStatus status, Pageable pageable) {
        return userJpaRepository.searchUsers(keyword, role, status, pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
