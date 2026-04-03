package com.da2joburereung.userservice.user.infrastructure;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
//    Page<User> findAllByDeletedAtisNull(Pageable pageable);
    Optional<User> findByUserIdAndDeletedAtIsNull(UUID userId);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.deletedAt IS NULL
          AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:role IS NULL OR u.role = :role)
          AND (:status IS NULL OR u.status = :status)
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
