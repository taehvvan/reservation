package com.example.backend.login.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDTO {
    private Integer id;
    private String name;
    private String email;
    private String role;
}