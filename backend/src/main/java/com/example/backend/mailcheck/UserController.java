package com.example.backend.mailcheck;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final MailService mailService;

    public UserController(MailService mailService){
        this.mailService = mailService;
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestBody EmailCheckRequest request){
        if (request.getEmail() == null || !request.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,4}$")) {
            // 이메일 형식이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(Map.of("isDuplicate", false));
        }

        // 서비스 계층을 통해 이메일 중복 확인
        boolean isDuplicate = mailService.isEmailDuplicate(request.getEmail());
        
        // JSON 응답 생성 및 반환
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        
        return ResponseEntity.ok(response);
    }
}
