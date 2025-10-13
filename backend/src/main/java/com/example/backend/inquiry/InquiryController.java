package com.example.backend.inquiry;

import com.example.backend.login.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 등록 API

    /**
     * [신규] 회원용 문의 등록 API
     */
    @PostMapping("/member")
    public ResponseEntity<InquiryResponseDTO> createMemberInquiry(@RequestBody InquiryRequestDTO requestDTO, Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Integer userId = principalDetails.getUser().getId();
        return ResponseEntity.ok(inquiryService.createInquiry(requestDTO, userId));
    }

    /**
     * [신규] 비회원용 문의 등록 API
     */
    @PostMapping("/non-member")
    public ResponseEntity<InquiryResponseDTO> createNonMemberInquiry(@RequestBody InquiryRequestDTO requestDTO) {
        // userId 없이 서비스 호출
        return ResponseEntity.ok(inquiryService.createInquiry(requestDTO, null));
    }

    /**
     * [수정] 내 문의내역 조회 API
     * 기존 코드에서 이미 이 방식으로 구현되어 있었습니다.
     */
    @GetMapping("/my")
    public ResponseEntity<List<InquiryResponseDTO>> getMyInquiries(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Integer userId = principalDetails.getUser().getId();
        return ResponseEntity.ok(inquiryService.getInquiriesByUserId(userId));
    }


    // --- 나머지 코드는 동일 ---

    @GetMapping
    public ResponseEntity<List<InquiryResponseDTO>> getAllInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponseDTO> getInquiryById(@PathVariable Integer id) {
        return ResponseEntity.ok(inquiryService.getInquiryById(id));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<InquiryResponseDTO> addAnswer(@PathVariable Integer id, @RequestBody InquiryAnswerDTO answerDTO) {
        return ResponseEntity.ok(inquiryService.addAnswer(id, answerDTO));
    }

    @GetMapping("/board")
    public ResponseEntity<List<InquiryResponseDTO>> getInquiriesForBoard() {
        return ResponseEntity.ok(inquiryService.getInquiriesForBoard());
    }
}