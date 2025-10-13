package com.example.backend.register;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.coupon.CouponService;
import com.example.backend.login.dto.TokenResponse;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.login.security.jwt.JwtTokenProvider;

import jakarta.validation.Valid;


@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleService googleService;

    @Autowired
    private KakaoService kakaoService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CouponService couponService;

    public UserEntity registerUser(UserDTO userDto) {
        try {
            // DTO를 Entity로 변환
            UserEntity user = new UserEntity();
            user.setEmail(userDto.getEmail());
            user.setName(userDto.getName());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRole("ROLE_USER"); // 기본 역할 설정

            UserEntity registeredUser = userRepository.save(user);

            if (registeredUser != null) {
                couponService.issueWelcomeCoupon(registeredUser);
            }

            return registeredUser;
        } catch (Exception e) {
            System.err.println("회원가입 처리 중 오류: " + e.getMessage());
            return null; // 실패 시 null을 반환합니다.
        }
    }

    @Transactional
    public void registerManager(ManagerRegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 사용중 입니다.");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCompanyName(request.getCompanyName());
        user.setBusinessNumber(request.getBusinessNumber());
        user.setRole("ROLE_PENDING");

        userRepository.save(user);
    }

    public UserEntity kakaoLoginOrRegister(String code) throws Exception {
        String accessToken = kakaoService.getAccessToken(code);

        UserEntity kakaoUser = kakaoService.getUserInfo(accessToken);

        Optional<UserEntity> existingUser = userRepository.findByEmailAndSocial(kakaoUser.getEmail(), "kakao");

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            kakaoUser.setPassword(null);
            kakaoUser.setSocial("kakao");
            UserEntity newUser = userRepository.save(kakaoUser);
            couponService.issueWelcomeCoupon(newUser);
            return newUser;
        }
    }

    public void saveRefreshToken(UserEntity user, String refreshToken) {
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
     public TokenResponse refreshAccessToken(String refreshToken) {
        // 1. DB에서 리프레시 토큰으로 사용자 정보를 찾습니다.
        UserEntity user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        // 2. 사용자 정보를 기반으로 Authentication 객체를 생성합니다.
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);

        // 3. Authentication 객체를 사용하여 새로운 액세스 토큰을 생성합니다.
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        
        return new TokenResponse("Bearer", newAccessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirationInMilliSeconds());
    }
    
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("이메일 못 찾음"));
    }
    
    public UserEntity googleLoginOrRegister(String code) throws Exception {
        String idToken = googleService.getIdToken(code);
        UserEntity googleUser = googleService.getUserInfo(idToken);
    
        Optional<UserEntity> existingUser = userRepository.findByEmailAndSocial(googleUser.getEmail(), "google");
    
        return existingUser.orElseGet(() -> {
            googleUser.setPassword(null);
            googleUser.setSocial("google");
            UserEntity newUser = userRepository.save(googleUser);
            couponService.issueWelcomeCoupon(newUser);
            return newUser;
        });
    }

}