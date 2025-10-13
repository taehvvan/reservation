package com.example.backend.login;

import com.example.backend.login.security.PrincipalDetailsService;
import com.example.backend.login.security.jwt.JwtAuthenticationFilter;
import com.example.backend.login.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityContext {

    private final PrincipalDetailsService principalDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityContext(JwtTokenProvider jwtTokenProvider, PrincipalDetailsService principalDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.principalDetailsService = principalDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173", "http://localhost:8888", "http://172.16.15.93:5173",
                        "http://172.16.15.95:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, principalDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        "/api/auth/login",
                        "/api/manager-register", //매니저 회원가입
                        "/api/register",
                        "/api/kakao/login",      // 카카오 로그인 시작 URL
                        "/api/kakao/callback",   // 카카오 콜백 URL
                        //"/api/auth/social/kakao/callback",
                        "/api/send-code",
                        "/api/verify-code",
                        "/api/check-email",
                        "/api/auth/**", "/api/auth/me",
                        "/api/google/login",
                        "/api/google/callback",
                        "/api/detail",  
                        "/", "/api/search", "/api/search/**",
                        "/api/reservations/**",
                        "/api/payments/**",
                        "/payment-success",
                        "/payment-fail",
                        "/payment-callback",
                        "/hotel/**", "/landmark/**", "/heritage/**",
                        "/terms", "/privacy",
                        "/accommodations", "/landmarks", "/heritage",
                        "/checkout-guest",
                        "/images/**", "/css/**", "/js/**", "/error",
                        "/api/request-reset-password", "/api/verify-code-for-password-reset",
                        "/api/reset-password", "/mypage/reservations",
                        "/api/rooms/**", "/api/services/**", "/api/mchart/**",
                        "/api/inquiries/public", "/api/inquiries/board"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/inquiries/non-member").permitAll()
                .requestMatchers("/api/auth/me").hasAnyRole("USER","MANAGER","ADMIN")
                .requestMatchers("/api/user/**", "/api/mypage", "/mypage/**", 
                "/api/wishlist", "/api/wishlist/**", "/api/inquiries/my").hasRole("USER")
                .requestMatchers("/api/manager/**", "/api/sales/**").hasRole("MANAGER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
               
                .anyRequest().authenticated()
            )
            // ✅ Bean으로 등록된 필터 인스턴스를 사용
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}