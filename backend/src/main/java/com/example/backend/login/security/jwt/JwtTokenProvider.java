package com.example.backend.login.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Collections;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.accessExpiration}")
    private long accessTokenExpirationInMs;

    @Value("${app.jwt.refreshExpiration}")
    private long refreshTokenExpirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 액세스 토큰 생성
    public String generateAccessToken(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserEntity user = principalDetails.getUser();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationInMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .claim("u_id", user.getId())
                .claim("businessNumber", user.getBusinessNumber())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    // 리프레시 토큰 생성
    public String generateRefreshToken(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserEntity user = principalDetails.getUser();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("u_id", user.getId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    // JWT에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        Integer uId = claims.get("u_id", Integer.class);
        String businessNumber = claims.get("businessNumber", String.class);

        UserEntity user = new UserEntity();
        user.setId(uId);
        user.setEmail(username);
        user.setRole(role);
        user.setBusinessNumber(businessNumber);

        PrincipalDetails principalDetails = new PrincipalDetails(user);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);
    }

    // JWT에서 이메일(사용자명) 추출
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // Assuming user ID is stored as "id" in the JWT payload
        return claims.get("u_id", Integer.class);
    
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }



    // 액세스 토큰 만료 시간 조회
    public Long getAccessTokenExpirationInMilliSeconds() {
        return accessTokenExpirationInMs;
    }

    // 리프레시 토큰 만료 시간 조회
    public Long getRefreshTokenExpirationInMilliSeconds() {
        return refreshTokenExpirationInMs;
    }
}