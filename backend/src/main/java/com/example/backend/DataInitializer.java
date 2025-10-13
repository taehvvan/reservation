package com.example.backend;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 관리자 이메일이 이미 존재하는지 확인합니다.
            if (!userRepository.findByEmail("admin@gmail.com").isPresent()) {
                // 관리자 계정 생성
                UserEntity admin = UserEntity.builder()
                        .name("관리자")
                        .email("admin@gmail.com")
                        // 중요: 비밀번호는 반드시 암호화하여 저장해야 합니다.
                        .password(passwordEncoder.encode("qwer123@"))
                        .role("ROLE_ADMIN")
                        .build();
                
                userRepository.save(admin);
                System.out.println(">>> 관리자 계정이 생성되었습니다: admin@example.com");
            }
        };
    }
}