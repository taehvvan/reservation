package com.example.backend.mypage;

import lombok.Getter;

@Getter
public class PasswordChangeRequestDTO {
    private String oldPassword;
    private String newPassword;

}