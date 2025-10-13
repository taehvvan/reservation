package com.example.backend.admin;

import com.example.backend.review.ReviewDeletionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // [추가]

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping("/deletion-requests")
    public ResponseEntity<List<ReviewDeletionRequestDTO>> getDeletionRequests() {
        List<ReviewDeletionRequestDTO> requests = adminReviewService.getAllDeletionRequests();
        return ResponseEntity.ok(requests);
    }

    @DeleteMapping("/deletion-requests/{requestId}")
    public ResponseEntity<Void> approveDeletion(@PathVariable Long requestId) {
        try {
            adminReviewService.approveDeletionRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // [추가] 리뷰 삭제 요청 반려 API 엔드포인트
    @PostMapping("/deletion-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectDeletion(@PathVariable Long requestId, @RequestBody Map<String, String> payload) {
        try {
            String reason = payload.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            adminReviewService.rejectDeletionRequest(requestId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}