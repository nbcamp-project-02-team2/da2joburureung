package com.da2joburereung.userservice.user.infrastructure;

import com.da2joburereung.userservice.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
}
