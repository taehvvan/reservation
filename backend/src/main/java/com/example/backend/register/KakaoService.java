package com.example.backend.register;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class KakaoService {
    
    @Value("${kakao.auth.client-id}")
    private String clientId;

    @Value("${kakao.auth.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public KakaoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        
        String response = webClient.post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("redirect_uri", redirectUri)
                .with("code", code))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("액세스 토큰 파싱 오류", e);
        }
    }

    public UserEntity getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        
        String response = WebClient.create()
            .get()
            .uri(userInfoUrl)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            JsonNode profile = kakaoAccount.get("profile");

            String email = kakaoAccount.get("email").asText();
            String name = profile.get("nickname").asText();
            
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.setName(name);
            user.setSocial("kakao");
            return user;
        } catch (Exception e) {
            throw new RuntimeException("사용자 정보 파싱 오류", e);
        }
    }
}