package com.example.backend.inquiry;

import com.example.backend.register.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String authorName;

    // 비회원 문의 시 연락받을 이메일
    private String contactEmail;

    // 답변 상태 (PENDING, ANSWERED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "u_id")
    private UserEntity user; // 회원 문의일 경우 user 정보 연결

    @Column(nullable = false)
    private boolean secret = false;

    @Column(length = 2000)
    private String answer;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime answeredAt;

    public boolean isMember() {
        return this.user != null;
    }

    
}