package com.example.backend.manager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.login.security.PrincipalDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mchart")
@RequiredArgsConstructor
public class MchartController {
    private final MchartService mchartService;

    @GetMapping("/my-info")
    public ResponseEntity<MchartResponseDTO> getMyInfo(
            @AuthenticationPrincipal PrincipalDetails principal) {

        Integer userId = principal.getUser().getId();
        MchartResponseDTO dto = mchartService.getDashboard(userId);
        return ResponseEntity.ok(dto);
    }
}