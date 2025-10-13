package com.example.backend.review;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.search.ReviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewDTO reviewDto,
                                                  @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = principalDetails.getUser().getId();
        ReviewDTO savedReview = reviewService.saveReview(reviewDto, userId);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = principalDetails.getUser().getId();
        List<ReviewDTO> reviews = reviewService.findReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }
}