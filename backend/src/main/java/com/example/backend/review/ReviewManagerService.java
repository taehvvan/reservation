package com.example.backend.review;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity; // [추가]
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelRepository;
import com.example.backend.search.Review;
import com.example.backend.search.ReviewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewManagerService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final ReviewDeletionRequestRepository deletionRequestRepository;

    public List<Review> findReviewsByHotelId(Long hotelId) {
        return reviewRepository.findByHotel_hIdOrderByCreatedAtDesc(hotelId);
    }

    @Transactional(readOnly = true)
    public List<Review> findReviewsByManager(PrincipalDetails principalDetails) {
        UserEntity manager = principalDetails.getUser();
        
        // 1. 매니저가 관리하는 모든 호텔 목록을 가져옵니다.
        List<Hotel> hotels = hotelRepository.findAllByUser(manager);

        // 2. 관리하는 호텔이 없는 경우 예외 처리
        if (hotels.isEmpty()) {
            throw new EntityNotFoundException("해당 매니저에게 할당된 호텔을 찾을 수 없습니다.");
        }

        // 3. 모든 호텔의 ID 목록을 추출합니다.
        List<Long> hotelIds = hotels.stream()
                                    .map(Hotel::getHId)
                                    .collect(Collectors.toList());

        // 4. 호텔 ID 목록으로 모든 리뷰를 한 번에 조회합니다.
        //    (ReviewRepository에 findByHotel_hIdIn 메소드가 필요합니다.)
        return reviewRepository.findByHotel_hIdIn(hotelIds); 
    }

    @Transactional
    public Review addReplyToReview(Long reviewId, String replyContent, PrincipalDetails principalDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        review.setReply(replyContent);
        review.setRepliedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public void requestReviewDeletion(Long reviewId, String reason, PrincipalDetails principalDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        // [추가] 로그인한 매니저 정보를 가져옵니다.
        UserEntity manager = principalDetails.getUser();

        deletionRequestRepository.findByReviewReviewIdAndStatus(reviewId, "PENDING").ifPresent(req -> {
            throw new IllegalStateException("이미 처리 중인 삭제 요청이 존재합니다.");
        });

        ReviewDeletionRequest request = new ReviewDeletionRequest();
        request.setReview(review);
        request.setReason(reason);
        request.setManager(manager); // [추가] 요청 객체에 매니저 정보를 설정합니다.
        
        deletionRequestRepository.save(request);
    }
}