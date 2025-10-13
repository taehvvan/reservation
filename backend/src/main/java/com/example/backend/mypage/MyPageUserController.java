package com.example.backend.mypage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.mailcheck.VerificationService;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;

@RestController
@RequestMapping("/api/users")
public class MyPageUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/info") //로그인한 유저 정보 가져옴
    public ResponseEntity<UserEntity> getUserInfo(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("유저를 찾지 못했습니다."));

        return ResponseEntity.ok(user);
    }

    @PutMapping("phone")//전화번호 입력
    public ResponseEntity<Map<String, String>> updatePhoneNumber(@RequestBody Map<String,String> request ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String newPhone = request.get("phone");

        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾지 못했습니다."));

        user.setPhone(newPhone);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "전화번호가 성공적으로 변경되었습니다."));
    }

    // [수정] 1. 기존 비밀번호 확인 후 인증코드 발송 (기존과 동일)
    @PostMapping("/password/verify-and-send-code")
    public ResponseEntity<Map<String, String>> verifyPasswordAndSendCode(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String oldPassword = request.get("oldPassword");

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾지 못했습니다."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "기존 비밀번호가 일치하지 않습니다."));
        }

        try {
            verificationService.sendVerificationCode(email);
            return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "이메일 전송에 실패했습니다."));
        }
    }

    // [추가] 2. 인증 코드를 확인하고 임시 토큰(resetToken)을 발급하는 엔드포인트
    @PostMapping("/password/verify-code")
    public ResponseEntity<Map<String, String>> verifyCodeForPasswordChange(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String code = request.get("code");

        boolean isVerified = verificationService.verifyCode(email, code);

        if (isVerified) {
            String resetToken = verificationService.createResetToken(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "인증이 완료되었습니다. 새 비밀번호를 입력해주세요.");
            response.put("resetToken", resetToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 올바르지 않거나 만료되었습니다."));
        }
    }

    // [수정] 3. 임시 토큰(resetToken)과 새 비밀번호로 최종 변경
    @PutMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPasswordWithToken(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        // 비밀번호 유효성 검사
        if (newPassword == null || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다."));
        }

        try {
            verificationService.resetPassword(resetToken, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}