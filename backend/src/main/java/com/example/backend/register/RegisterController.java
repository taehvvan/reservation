package com.example.backend.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.login.dto.TokenResponse;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public RegisterController(UserService userService, JwtTokenProvider jwtTokenProvider) { // [추가] 생성자에 주입
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> registerUser(@Valid @RequestBody UserDTO userDto) {
        // [수정] UserService는 이제 UserEntity를 반환합니다.
        UserEntity registeredUser = userService.registerUser(userDto);

        if (registeredUser != null) {
            // 회원가입 성공 시, 바로 토큰을 발급하는 로직을 추가합니다.
            PrincipalDetails principalDetails = new PrincipalDetails(registeredUser);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(registeredUser.getRole()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);

            // 액세스 토큰과 리프레시 토큰을 생성합니다.
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // 생성된 리프레시 토큰을 DB에 저장합니다.
            userService.saveRefreshToken(registeredUser, refreshToken);

            // 최종적으로 TokenResponse 객체를 생성하여 클라이언트에 반환합니다.
            TokenResponse tokenResponse = new TokenResponse("Bearer", accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirationInMilliSeconds());

            return ResponseEntity.ok(tokenResponse);
        } else {
            // 회원가입 실패 시, 400 Bad Request 응답을 보냅니다.
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/manager-register")
    public ResponseEntity<Map<String, Boolean>> registerManager(@Valid @RequestBody ManagerRegisterRequest request) {
        try {
            userService.registerManager(request);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("success", false));
        }
    }
}