package com.example.backend.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenResponse {

    private String grantType;          // 예: Bearer
    private String accessToken;        // 액세스 토큰
    private Long accessTokenExpiresIn; // 액세스 토큰 만료 시간(ms)
    private String refreshToken;       // 리프레시 토큰

    public TokenResponse(String grantType, String accessToken, String refreshToken, Long accessTokenExpiresIn) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }
}
