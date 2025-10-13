package com.example.backend.inquiry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryResponseDTO {
    private Integer id;
    private String title;
    private String content;
    private String authorName;
    private String phoneNumber;
    private String email;
    private InquiryStatus status;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
    private boolean isMember;
    private boolean secret;
}
