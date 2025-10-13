package com.example.backend.reservation;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity;
import com.example.backend.search.RoomAvailabilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RoomAvailabilityService roomAvailabilityService;

    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody PaymentRequest request,
                                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        System.out.println("---------- [PAYMENT-CONTROLLER] 결제 완료 요청 수신 ----------");
        System.out.println("수신된 DTO: " + request);

        /*
        // Spring Security 컨텍스트에서 인증된 사용자 정보를 가져옴 (가장 신뢰할 수 있는 방법)
        UserEntity user = (principalDetails != null) ? principalDetails.getUser() : null;
        */
        try {

        // 1. 결제 완료 처리
            PaymentResponseDTO responseDTO = paymentService.completePayment(request, principalDetails);

        // 성공 응답 반환
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 데이터 불일치 등 예측 가능한 오류인 경우
            System.err.println("[PAYMENT ERROR] " + e.getMessage());
            // 프론트엔드가 오류 원인을 알 수 있도록 에러 메시지를 JSON 형태로 반환
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
                    
        } catch (Exception e) {
            // 데이터베이스 오류, NullPointerException 등 예측하지 못한 심각한 오류인 경우
            System.err.println("[CRITICAL PAYMENT ERROR] " + e.getMessage());
            e.printStackTrace(); // 서버 로그에 전체 스택 트레이스를 출력
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "결제를 처리하는 중 서버에 문제가 발생했습니다."));
        }
    }

    // 비회원 예약 확인
    @GetMapping("/guest")
    public ResponseEntity<?> checkGuestReservation(@RequestParam String orderId,
                                                   @RequestParam String phone) {
        ReservationResponseDTO dto = paymentService.findReservationForGuest(orderId, phone);
        if (dto != null) return ResponseEntity.ok(dto);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("예약 정보를 찾을 수 없습니다.");
    }
}