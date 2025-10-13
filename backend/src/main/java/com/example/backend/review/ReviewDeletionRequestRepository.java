package com.example.backend.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewDeletionRequestRepository extends JpaRepository<ReviewDeletionRequest, Long> {
    // 모든 삭제 요청을 최신순으로 조회
    List<ReviewDeletionRequest> findAllByOrderByIdDesc();
    
    // 특정 리뷰에 대해 PENDING 상태인 요청이 있는지 확인
    Optional<ReviewDeletionRequest> findByReviewReviewIdAndStatus(Long reviewId, String status);
}