package com.example.backend.mailcheck;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.register.UserRepository;

@RestController
@RequestMapping("/api")
public class EmailSendController {

    //private final EmailSendService emailSendService;
    private final VerificationService verificationService;

    private final UserRepository userRepository;

    /* 
    public EmailSendContoller(EmailSendService emailSendService, VerificationService verificationService){
        this.emailSendService = emailSendService;
        this.verificationService = verificationService;
    }
    */

    public EmailSendController(VerificationService verificationService, UserRepository userRepository){
        this.verificationService = verificationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일 주소를 입력해주세요."));
        }

        try {
            // VerificationService를 통해 인증번호 생성 및 이메일 전송
            verificationService.sendVerificationCode(email);
            return ResponseEntity.ok(Map.of("message", "인증번호가 이메일로 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "이메일 전송에 실패했습니다."));
        }
    }

    

    // ---

    // 이메일 인증번호 확인 API
    // 요청 본문: { "email": "user@example.com", "code": "123456" }
    @PostMapping("/request-reset-password")
    public ResponseEntity<Map<String, String>> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일 주소를 입력해주세요."));
        }
        
        // 1단계: 이메일 존재 여부 확인
        if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "등록되지 않은 이메일입니다."));
        }

        try {
            // 2단계: 인증번호 전송
            verificationService.sendVerificationCode(email);
            return ResponseEntity.ok(Map.of("message", "비밀번호 재설정을 위한 인증번호가 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "이메일 전송에 실패했습니다."));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일 또는 인증번호를 입력해주세요."));
        }

        // VerificationService를 통해 인증번호 일치 여부 확인
        boolean isVerified = verificationService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }
    
    @PostMapping("/verify-code-for-password-reset")
    public ResponseEntity<Map<String, String>> verifyCodeReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일 또는 인증번호를 입력해주세요."));
        }

        // 3단계: 인증번호 일치 여부 확인
        boolean isVerified = verificationService.verifyCode(email, code);

        if (isVerified) {
            // 4단계: 인증 성공 시, 비밀번호 재설정 토큰 생성
            String resetToken = verificationService.createResetToken(email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "이메일 인증이 완료되었습니다. 비밀번호를 재설정할 수 있습니다.");
            response.put("resetToken", resetToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        if (resetToken == null || newPassword == null || resetToken.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "토큰과 새 비밀번호를 모두 입력해주세요."));
        }

        try {
            verificationService.resetPassword(resetToken, newPassword);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
