package com.example.backend.login;

import com.example.backend.login.dto.LoginRequest;
import com.example.backend.login.dto.TokenResponse;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.PrincipalDetailsService;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.login.security.jwt.LoginDTO;

import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;

import jakarta.validation.Valid;

import com.example.backend.register.UserService;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PrincipalDetailsService principalDetailsService;

    @Autowired
    public LoginController(@Lazy AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            UserService userService,
            PrincipalDetailsService principalDetailsService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userService = userService;
        this.principalDetailsService = principalDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. 이메일을 기반으로 사용자 정보를 불러옵니다.
        UserDetails userDetails = principalDetailsService.loadUserByUsername(loginRequest.getEmail());

        // 2. 비밀번호가 일치하는지 확인합니다.
        if (!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // ▼▼▼▼▼▼▼▼▼▼ [ 여기가 핵심 수정 부분입니다 ] ▼▼▼▼▼▼▼▼▼▼

        // 3. UserDetails를 기반으로 Authentication 객체를 생성합니다.
        //    (주의: userDetails가 PrincipalDetails 타입이어야 getUser() 호출 가능)
        PrincipalDetails principalDetails = (PrincipalDetails) userDetails;
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities());

        // 4. SecurityContext에 인증 정보를 설정합니다.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 5. 생성된 Authentication 객체를 사용하여 토큰을 발급합니다.
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // ▲▲▲▲▲▲▲▲▲▲ [ 수정 완료 ] ▲▲▲▲▲▲▲▲▲▲

        // 6. 리프레시 토큰을 DB에 저장합니다.
        UserEntity user = principalDetails.getUser();
        userService.saveRefreshToken(user, refreshToken);

        // 7. 토큰을 클라이언트에 반환합니다.
        return ResponseEntity.ok(new TokenResponse("Bearer", accessToken, refreshToken,
                jwtTokenProvider.getAccessTokenExpirationInMilliSeconds()));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginDTO> getCurrentUser(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String email = jwtTokenProvider.getUsernameFromToken(token);

        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        LoginDTO dto = new LoginDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // 리프레시 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일을 찾을 수 없습니다."));

        // 토큰에서 사용자 정보 추출
        UserDetails userDetails = principalDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        // TokenResponse 객체 생성
        TokenResponse tokenResponse = new TokenResponse(
                "Bearer",
                newAccessToken,
                refreshToken, // 기존 리프레시 토큰 그대로 반환
                jwtTokenProvider.getAccessTokenExpirationInMilliSeconds());

        return ResponseEntity.ok(tokenResponse);
    }
}