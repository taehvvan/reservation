package com.example.backend.admin;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(String email, PasswordChangeRequest request) {
        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증된 관리자 정보를 찾을 수 없습니다."));

        // 1. 현재 비밀번호가 일치하는지 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호를 암호화하여 저장
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(admin);
    }
}