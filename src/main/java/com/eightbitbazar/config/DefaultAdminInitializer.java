package com.eightbitbazar.config;

import com.eightbitbazar.application.port.out.PasswordEncoder;
import com.eightbitbazar.application.port.out.UserRepository;
import com.eightbitbazar.domain.user.Address;
import com.eightbitbazar.domain.user.Role;
import com.eightbitbazar.domain.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final String DEFAULT_EMAIL = "admin@8bitbazar.com";
    private static final String DEFAULT_NICKNAME = "admin";
    private static final String DEFAULT_FULL_NAME = "Administrador 8BitBazar";
    private static final String DEFAULT_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(DEFAULT_EMAIL)) {
            return;
        }

        User admin = new User(
            null,
            DEFAULT_EMAIL,
            passwordEncoder.encode(DEFAULT_PASSWORD),
            DEFAULT_NICKNAME,
            DEFAULT_FULL_NAME,
            null,
            null,
            Role.ADMIN,
            true,
            Address.empty(),
            LocalDateTime.now(),
            null
        );

        userRepository.save(admin);
        log.info("Usuário admin default criado: email={}", DEFAULT_EMAIL);
    }
}
