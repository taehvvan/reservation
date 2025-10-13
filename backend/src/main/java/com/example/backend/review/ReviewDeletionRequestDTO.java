package com.example.backend.review;

import com.example.backend.search.ReviewDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDeletionRequestDTO {
    private Long id;
    private ReviewDTO review;
    private String reason;
    private LocalDateTime requestedAt;
    private String status;
    private String rejectionReason;

    public ReviewDeletionRequestDTO(ReviewDeletionRequest entity) {
        this.id = entity.getId();
        this.review = new ReviewDTO(entity.getReview());
        this.reason = entity.getReason();
        this.requestedAt = entity.getRequestedAt();
        this.status = entity.getStatus();
        this.rejectionReason = entity.getRejectionReason();
    }
}