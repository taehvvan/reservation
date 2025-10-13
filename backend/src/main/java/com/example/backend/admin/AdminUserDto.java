package com.example.backend.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 관리자 페이지의 사용자 목록에 보여줄 데이터를 담는 DTO(Data Transfer Object)입니다.
 */
@Data
@AllArgsConstructor
public class AdminUserDto {
    private Integer id;
    private String name;
    private String email;
    private String role;
    private String social;
private String companyName; // [추가] 상호명 필드
    private String businessNumber; 


}