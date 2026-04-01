package com.da2joburereung.userservice.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByUsername(String username);

    Optional<User> findActiveById(UUID userId);
    Optional<User> findActiveByUsername(String username);
    boolean existsByUsername(String username);
}
