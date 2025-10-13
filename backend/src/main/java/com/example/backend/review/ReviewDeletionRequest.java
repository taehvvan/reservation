package com.example.backend.review;

import com.example.backend.register.UserEntity; // [추가]
import com.example.backend.search.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_deletion_request")
@Getter
@Setter
public class ReviewDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    // [추가] 요청을 보낸 매니저 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private UserEntity manager;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; 
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}