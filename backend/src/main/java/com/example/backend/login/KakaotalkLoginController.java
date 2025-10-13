package com.example.backend.login;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.login.dto.TokenResponse;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserService;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class KakaotalkLoginController {

    //@Autowired
    private UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public KakaotalkLoginController(UserService userService, JwtTokenProvider jwtTokenProvider){
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Value("${kakao.auth.client-id}")
    private String clientId;

    @Value("${kakao.auth.redirect-uri}")
    private String redirectUri;

     @GetMapping("/kakao/login")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException, java.io.IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
            + "?client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&response_type=code";
        
        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam("code") String code) {
        try {
            UserEntity user = userService.kakaoLoginOrRegister(code);

            PrincipalDetails principalDetails = new PrincipalDetails(user);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);

            // 액세스 토큰 + 리프레시 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // DB에 리프레시 토큰 저장
            userService.saveRefreshToken(user, refreshToken);

            return ResponseEntity.ok(
                    new TokenResponse(
                            "Bearer",
                            accessToken,
                            refreshToken,
                            jwtTokenProvider.getAccessTokenExpirationInMilliSeconds()
                    )
            );
        } catch (Exception e) {
            System.err.println("카카오 로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
class ResponseMessage {
    private boolean success;
    private String message;

    public ResponseMessage() {}

    public ResponseMessage(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
