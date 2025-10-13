package com.example.backend.review;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.search.Review;
import com.example.backend.search.ReviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager/reviews")
@RequiredArgsConstructor
public class ReviewManagerController {

    private final ReviewManagerService reviewManagerService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getReviewsForCurrentManager(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        // 1. 로그인한 사용자 정보가 없으면 401 Unauthorized 응답
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 2. 서비스 레이어를 호출하여 리뷰 목록을 가져옴
        List<Review> reviews = reviewManagerService.findReviewsByManager(principalDetails);
        
        // 3. Review 엔티티 목록을 ReviewDTO 목록으로 변환
        List<ReviewDTO> reviewDTOs = reviews.stream().map(ReviewDTO::new).collect(Collectors.toList());
        
        // 4. DTO 목록을 성공 응답(200 OK)으로 반환
        return ResponseEntity.ok(reviewDTOs);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByHotelId(@PathVariable Long hotelId) {
        List<Review> reviews = reviewManagerService.findReviewsByHotelId(hotelId);
        List<ReviewDTO> reviewDTOs = reviews.stream().map(ReviewDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(reviewDTOs);
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewDTO> addReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) { return ResponseEntity.status(401).build(); }
        String replyContent = payload.get("reply");
        Review updatedReview = reviewManagerService.addReplyToReview(reviewId, replyContent, principalDetails);
        return ResponseEntity.ok(new ReviewDTO(updatedReview));
    }

    @PostMapping("/{reviewId}/request-deletion")
    public ResponseEntity<Map<String, String>> requestDeletion(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) { return ResponseEntity.status(401).build(); }
        String reason = payload.get("reason");
        try {
            reviewManagerService.requestReviewDeletion(reviewId, reason, principalDetails);
            return ResponseEntity.ok(Map.of("message", "삭제 요청이 성공적으로 접수되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage())); // 409 Conflict
        }
    }
}