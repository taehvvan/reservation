package com.example.backend.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.backend.login.dto.TokenResponse;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.register.GoogleService;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GoogleController {

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleService googleService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${google.auth.client-id}")
    private String googleClientId;

    @Value("${google.auth.redirect-uri}")
    private String googleRedirectUri;

    @Value("${app.frontend-success-redirect}")
    private String FRONTEND_SUCCESS_REDIRECT_URL;

    @Value("${app.frontend-failure-redirect}")
    private String FRONTEND_FAILURE_REDIRECT_URL;

    @Value("${app.frontend}")
    private String FROUNTEND;
    
    @GetMapping("/google/login")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + googleClientId
            + "&redirect_uri=" + googleRedirectUri
            + "&response_type=code"
            + "&scope=openid%20email%20profile";
        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/google/callback")
    public RedirectView googleLogin(@RequestParam("code") String code) {
        try {
            UserEntity user = googleService.googleLoginOrRegister(code);

            PrincipalDetails principalDetails = new PrincipalDetails(user);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);

            // 액세스 토큰 + 리프레시 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // DB에 리프레시 토큰 저장
            userService.saveRefreshToken(user, refreshToken);

            // 토큰 반환 (프론트엔드에서 처리)
           String redirectUrl = FROUNTEND+"/google/callback" + 
                             "?access_token=" + accessToken + 
                             "&refresh_token=" + refreshToken;
            
            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            System.err.println("Google 로그인 실패: " + e.getMessage());
            return new RedirectView(FROUNTEND + "/error"); 
        }
    }
}