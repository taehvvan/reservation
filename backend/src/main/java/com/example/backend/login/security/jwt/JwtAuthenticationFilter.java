package com.example.backend.login.security.jwt;

import java.io.IOException;

import com.example.backend.login.security.PrincipalDetailsService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final PrincipalDetailsService principalDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, PrincipalDetailsService principalDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.principalDetailsService = principalDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        System.out.println("JwtAuthenticationFilter 실행됨");
        String path = request.getRequestURI();

        // 로그인, 회원가입, 이미지, css, js, 검색 등 JWT 검사 제외 URL
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/register") ||
            path.startsWith("/api/send-code") || path.startsWith("/api/verify-code") || 
            path.startsWith("/api/check-email") || path.startsWith("/api/search") || path.startsWith("/api/search/**") || 
            path.startsWith("/api/detail") || path.startsWith("/api/kakao/login") ||
            path.startsWith("/api/kakao/callback") || path.startsWith("/api/google/login") ||
            path.startsWith("/api/google/callback") || path.startsWith("/api/request-reset-password") || 
            path.startsWith("/api/verify-code-for-password-reset") || path.startsWith("/api/reset-password") ||
            path.startsWith("/api/reservations") || path.startsWith("/api/payments/complete") ||
            path.startsWith("/images/") || path.startsWith("/css/") || path.startsWith("/js/") ||
            path.startsWith("/api/manager-register") || path.startsWith("/api/rooms") || 
            path.startsWith("/api/inquiries/board") || path.startsWith("/api/inquiries/non-member")) {
            
                filterChain.doFilter(request, response);
            return;
        }

        try {
            System.out.println("Request URI: " + request.getRequestURI());
            System.out.println("Token: " + resolveToken(request));

            // 1. 요청 헤더에서 JWT 토큰 추출
            String jwt = resolveToken(request);

            // 2. 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. 토큰에서 사용자 이메일(고유 식별자) 추출
                String email = jwtTokenProvider.getUsernameFromToken(jwt);
                
                // 4. 이메일을 사용하여 DB에서 UserDetails(PrincipalDetails) 객체를 로드
                // 이 객체 안에는 UserEntity의 모든 정보가 포함되어 있습니다.
                UserDetails userDetails = principalDetailsService.loadUserByUsername(email);
                
                // 5. PrincipalDetails 객체를 사용하여 Authentication 객체 생성
                // 이 과정을 통해 SecurityContext에 uId, name 등이 포함된 완전한 사용자 정보가 저장됩니다.
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                // 6. SecurityContext에 생성된 Authentication 객체를 저장
                // 이제부터 컨트롤러에서 @AuthenticationPrincipal을 통해 PrincipalDetails 객체를 주입받을 수 있습니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Principal: " + authentication.getPrincipal());
                System.out.println("Authorities: " + authentication.getAuthorities());
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing JWT token");
                return;
            }

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to authenticate JWT token: " + e.getMessage());
            return;
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}