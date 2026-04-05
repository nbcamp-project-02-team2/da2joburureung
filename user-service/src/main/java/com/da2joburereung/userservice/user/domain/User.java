package com.da2joburereung.userservice.user.domain;

import common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true, length = 10)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "slack_id", nullable = false, length = 100)
    private String slackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "company_id")
    private UUID companyId;

    @Builder
    private User(
            UUID userId,
            String username,
            String password,
            String name,
            String email,
            String slackId,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.slackId = slackId;
        this.role = role;
        this.status = status;
        this.hubId = hubId;
        this.companyId = companyId;
    }

    public static User createPendingUser(
            String username,
            String password,
            String name,
            String email,
            String slackId,
            UserRole role,
            UUID hubId,
            UUID companyId
    ) {
        return User.builder()
                .userId(UUID.randomUUID())
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .slackId(slackId)
                .role(role)
                .status(UserStatus.PENDING)
                .hubId(hubId)
                .companyId(companyId)
                .build();
    }

    public static User createApprovedAdmin(
            String username,
            String password,
            String name,
            String slackId
    ) {
        return User.builder()
                .userId(UUID.randomUUID())
                .username(username)
                .password(password)
                .name(name)
                .email("master@email.com")
                .slackId(slackId)
                .role(UserRole.MASTER)
                .status(UserStatus.APPROVED)
                .hubId(null)
                .companyId(null)
                .build();
    }

    public void approve() {
        this.status = UserStatus.APPROVED;
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public void delete(String deletedBy) {
        this.softDelete(deletedBy);
    }

}