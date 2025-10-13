package com.example.backend.admin;

import com.example.backend.review.ReviewDeletionRequest;
import com.example.backend.review.ReviewDeletionRequestDTO;
import com.example.backend.review.ReviewDeletionRequestRepository;
import com.example.backend.search.Review;
import com.example.backend.search.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewService {

    private final ReviewDeletionRequestRepository deletionRequestRepository;
    private final ReviewRepository reviewRepository;

    public List<ReviewDeletionRequestDTO> getAllDeletionRequests() {
        return deletionRequestRepository.findAllByOrderByIdDesc().stream()
                .map(ReviewDeletionRequestDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveDeletionRequest(Long requestId) {
        ReviewDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 요청을 찾을 수 없습니다. ID: " + requestId));
        
        Review reviewToDelete = request.getReview();

        if (reviewToDelete != null) {
            deletionRequestRepository.delete(request);
            reviewRepository.delete(reviewToDelete);
        } else {
            deletionRequestRepository.delete(request);
        }
    }

    // [추가] 리뷰 삭제 요청 반려 메소드
    @Transactional
    public void rejectDeletionRequest(Long requestId, String reason) {
        ReviewDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 요청을 찾을 수 없습니다. ID: " + requestId));

        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        deletionRequestRepository.save(request);
    }
}