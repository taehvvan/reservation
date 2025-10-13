package com.example.backend.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Service
public class GoogleService {
    
    
    private final WebClient webClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleIdTokenVerifier verifier;
    
    // ✅ @Value 필드를 final로 선언하여 불변성을 보장합니다.
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    // ✅ 생성자 파라미터로 @Value 값을 주입받습니다.
    public GoogleService(
        WebClient.Builder webClientBuilder, 
        UserRepository userRepository, 
        JwtTokenProvider jwtTokenProvider,
        @Value("${google.auth.client-id}") String clientId,
        @Value("${google.auth.client-secret}") String clientSecret,
        @Value("${google.auth.redirect-uri}") String redirectUri) {

        // ✅ 파라미터로 받은 값을 필드에 할당합니다.
        this.webClient = webClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        
        // ✅ 이제 clientId가 null이 아닌 실제 값으로 초기화됩니다.
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(Collections.singletonList(this.clientId))
            .build();
    }


    public String getIdToken(String code) {
        Map<String, Object> tokens = webClient.post()
                .uri("/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("redirect_uri", redirectUri)
                    .with("code", code))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    
        if (tokens == null || !tokens.containsKey("id_token")) {
            throw new IllegalArgumentException("Failed to retrieve ID token from Google.");
        }
        return (String) tokens.get("id_token");
    }
    
    // ✅ Mono<UserEntity> -> UserEntity로 반환 타입 변경
    public UserEntity getUserInfo(String idToken) throws GeneralSecurityException, IOException {
        GoogleIdToken googleIdToken = verifier.verify(idToken);
        if (googleIdToken != null) {
            Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.setName(name);
            user.setSocial("google");
            user.setRole("ROLE_USER");
            return user;
        } else {
            throw new IllegalArgumentException("Invalid ID Token.");
        }
    }

    // ✅ 최종 로그인/등록 메서드도 일반 객체 반환으로 변경
    public UserEntity googleLoginOrRegister(String code) throws Exception {
        String idToken = getIdToken(code);
        UserEntity googleUser = getUserInfo(idToken);
    
        return userRepository.findByEmailAndSocial(googleUser.getEmail(), "google")
                .orElseGet(() -> {
                    googleUser.setPassword(null);
                    googleUser.setRole("ROLE_USER");
                    googleUser.setSocial("google");
                    return userRepository.save(googleUser);
                });
    }
}