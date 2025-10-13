package com.example.backend.admin;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.mailcheck.EmailSendService;
import java.util.List;

/**
 * 관리자의 사용자 관리 기능에 대한 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final EmailSendService emailSendService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 컨트롤러로부터 받은 조건으로 사용자 목록을 조회합니다.
     * @param role 필터링할 사용자 역할 ('USER', 'MANAGER', 'ALL')
     * @param searchQuery 검색할 이름 또는 이메일
     * @return 조회된 사용자 DTO 리스트
     */
    public List<AdminUserDto> getUsers(String role, String searchQuery) {
        String roleFilter = (role == null || role.equalsIgnoreCase("ALL")) ? "ALL" : "ROLE_" + role.toUpperCase();
        String query = (searchQuery == null || searchQuery.isBlank()) ? null : searchQuery;
        
        return userRepository.findUsersForAdmin(roleFilter, query);
    }

    /**
     * 특정 ID를 가진 사용자를 데이터베이스에서 삭제합니다.
     * @param userId 삭제할 사용자의 ID
     */
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
     @Transactional
    public void approveManager(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));
        
        if (!"ROLE_PENDING".equals(user.getRole())) {
            throw new IllegalStateException("승인 대기 중인 사용자가 아닙니다.");
        }

        user.setRole("ROLE_MANAGER");

        // 4. 토큰 발급을 위한 Authentication 객체 생성
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities());

        // 5. Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 6. 생성된 Refresh Token을 UserEntity에 설정
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        // [추가] 역할 변경 후 승인 완료 이메일 발송
        emailSendService.sendManagerApprovalEmail(user.getEmail());
    }
}