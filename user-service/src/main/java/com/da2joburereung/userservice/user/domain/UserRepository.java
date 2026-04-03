package com.da2joburereung.userservice.user.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);

//    Page<User> findActiveUsers(Pageable pageable);

    Page<User> searchActiveUsers(String keyword, UserRole role, UserStatus status, Pageable pageable);

    Optional<User> findActiveById(UUID userId);
    Optional<User> findActiveByUsername(String username);
    boolean existsByUsername(String username);
}
