package com.example.backend.search;

import com.example.backend.register.UserEntity;
import com.example.backend.reservation.Reservation;
import com.example.backend.review.ReviewDeletionRequest; // [추가] import
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "h_id")
    private Hotel hotel;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "re_id", unique = true)
    private Reservation reservation;

    @Column(name = "image")
    private String image;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reply", columnDefinition = "TEXT")
    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    // [추가] 리뷰 삭제 요청과의 양방향 관계 설정
    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ReviewDeletionRequest deletionRequest;
}