package com.example.backend.search;

import com.example.backend.review.ReviewDeletionRequest; // [추가] import
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private Integer reservationId;
    private Long hId;
    private String hotelName;
    private String image;
    private Integer score;
    private String content;
    private String userName;
    private LocalDateTime createdAt;
    
    // 매니저 답글 필드
    private String reply;
    private LocalDateTime repliedAt;

    // [추가] 삭제 요청 상태와 반려 사유 필드
    private String deletionStatus;
    private String rejectionReason;

    public ReviewDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.image = review.getImage();
        this.score = review.getScore();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
        this.reply = review.getReply();
        this.repliedAt = review.getRepliedAt();

        if (review.getReservation() != null) {
            this.reservationId = review.getReservation().getReId();
        }

        if (review.getHotel() != null) {
            this.hId = review.getHotel().getHId();
            this.hotelName = review.getHotel().getHName();
        }
        
        if (review.getUser() != null) {
            this.userName = review.getUser().getName();
        }

        // [추가] 삭제 요청 정보 매핑
        ReviewDeletionRequest request = review.getDeletionRequest();
        if (request != null) {
            this.deletionStatus = request.getStatus();
            this.rejectionReason = request.getRejectionReason();
        }
    }
}