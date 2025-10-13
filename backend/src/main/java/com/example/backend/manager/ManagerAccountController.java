package com.example.backend.manager;

import com.example.backend.admin.PasswordChangeRequest;
import com.example.backend.login.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/account")
@RequiredArgsConstructor
public class ManagerAccountController {

    private final ManagerAccountService managerAccountService;

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody PasswordChangeRequest request) {
        
        try {
            managerAccountService.changePassword(principalDetails.getUsername(), request);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}