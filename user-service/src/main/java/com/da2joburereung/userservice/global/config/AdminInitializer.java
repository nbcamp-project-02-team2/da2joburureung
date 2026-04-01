package com.da2joburereung.userservice.global.config;

import com.da2joburereung.userservice.user.domain.User;
import com.da2joburereung.userservice.user.domain.UserRepository;
import com.da2joburereung.userservice.user.domain.UserRole;
import com.da2joburereung.userservice.user.domain.UserStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminUsername = "master1";

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("기본 관리자 계정이 이미 존재합니다. username={}", adminUsername);
            return;
        }

        User admin = User.createApprovedAdmin(
                adminUsername,
                passwordEncoder.encode("Admin123!"),
                "기본관리자",
                "U000ADMIN"
        );

        userRepository.save(admin);
        log.info("기본 관리자 계정 생성 완료. username={}", adminUsername);
    }
}