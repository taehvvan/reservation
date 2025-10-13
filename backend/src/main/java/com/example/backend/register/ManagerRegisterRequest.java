package com.example.backend.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String companyName;
    private String businessNumber;
}
