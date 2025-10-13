package com.example.backend.mypage;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.reservation.ReservationService;
import com.example.backend.reservation.ReservationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponseDTO>> getMyReservations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (principalDetails == null) {
            return ResponseEntity.status(401).build(); // 비로그인 접근 차단
        }
        Integer userId = principalDetails.getUser().getId();

        System.out.println("[BACKEND-CONTROLLER] 사용자 ID '{}'의 예약 내역 조회를 시작합니다 : " + userId);

        System.out.println("### 최종 확인: 컨트롤러가 조회하려는 사용자의 ID는 [{}] 입니다 : " + userId);

        List<ReservationResponseDTO> reservations = reservationService.findMyReservations(userId);
        return ResponseEntity.ok(reservations);
    }
}
