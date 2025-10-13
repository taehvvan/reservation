package com.example.backend.reservation;

import com.example.backend.search.RoomAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.example.backend.login.security.PrincipalDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomAvailabilityService roomAvailabilityService;

    @PostMapping("/prepare")
    // ✅ ResponseEntity의 반환 타입을 ReservationPrepareResponse 로 변경
    public ResponseEntity<ReservationPrepareResponse> prepareReservation(@RequestBody ReservationRequest request) {

        System.out.println("---------- [BACKEND-CONTROLLER] 예약 준비 요청 수신 ----------");
        System.out.println("수신된 ReservationRequest DTO: " + request);
 
        // ✅ 수정된 서비스 메소드를 호출합니다.
        ReservationPrepareResponse response = reservationService.createReservation(request);
        
        // ✅ 프론트엔드가 사용하기 편한 형식의 DTO를 반환합니다.
        return ResponseEntity.ok(response);
    }

    // ... (다른 GET 메소드들은 기존과 동일) ...
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getUserReservations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        Integer userId = principalDetails.getUser().getId();
        List<ReservationResponseDTO> reservations = reservationService.findMyReservations(userId);
        
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReservationResponseDTO> getReservationByOrderId(@PathVariable String orderId) {
        try {
            ReservationResponseDTO reservation = reservationService.getReservationByOrderId(orderId);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/guest")
    public ResponseEntity<ReservationResponseDTO> getGuestReservationByOrderId(
            @RequestParam String orderId, @RequestParam String phone) {
        
        ReservationResponseDTO reservation = reservationService.findGuestReservationByOrderId(orderId, phone);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation);
    }

    // 예약 취소
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Integer reservationId) {
        try {
            Long canceledAmount = reservationService.cancelReservation(reservationId);
            if (canceledAmount == -1L) {
                 return ResponseEntity.ok(Map.of("message", "이미 취소된 예약입니다."));
            }
            // 성공 응답에 취소된 금액을 포함하여 보냅니다.
            return ResponseEntity.ok(Map.of(
                "message", "예약이 성공적으로 취소되었습니다.",
                "canceledAmount", canceledAmount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
        }
    }

    // 삭제 (일단 사용자 화면에서)
    @DeleteMapping("/{reservationId}/delete")
    public ResponseEntity<String> deleteCancelledReservation(@PathVariable Integer reservationId) {
        reservationService.markAsDeleted(reservationId);
        return ResponseEntity.ok("삭제 성공");
    }
}