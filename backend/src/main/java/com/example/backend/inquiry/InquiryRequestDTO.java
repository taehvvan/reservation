package com.example.backend.inquiry;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InquiryRequestDTO {
    private String title;
    private String content;
    private String authorName;
    private String contactEmail; // 비회원용
    private Integer userId; // 회원용

    private boolean secret;
}
