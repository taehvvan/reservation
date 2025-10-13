package com.example.backend.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 관리자(Admin)가 호텔을 관리하기 위한 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/admin/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}") 
public class AdminHotelController {

    private final AdminHotelService adminHotelService;

    /**
     * [수정된 부분]
     * '/all' 경로로 요청을 받아 모든 호텔 목록을 반환하는 API입니다.
     * 
     * @return 모든 호텔 DTO 리스트
     */
    @GetMapping("/all")
    public ResponseEntity<List<AdminHotelDto>> getAllHotels() {
        // 서비스 레이어에 "ALL"이라는 신호를 보내 모든 호텔을 조회하도록 합니다.
        return ResponseEntity.ok(adminHotelService.getHotels("ALL"));
    }

    /**
     * 특정 타입의 호텔 목록과 통계를 조회하는 API입니다.
     * 
     * @param type 쿼리 파라미터 (예: /api/admin/hotels?type=호텔)
     * @return 특정 타입의 호텔 DTO 리스트
     */
    @GetMapping
    public ResponseEntity<List<AdminHotelDto>> getHotelsByType(@RequestParam String type) {
        return ResponseEntity.ok(adminHotelService.getHotels(type));
    }

    /**
     * 호텔의 활성화 상태를 변경하는 API입니다.
     * 
     * @param hotelId URL 경로의 호텔 ID (예: /api/admin/hotels/15/status)
     * @param payload 요청 본문의 'active' 상태(true/false)
     * @return 성공 응답
     */
    @PutMapping("/{hotelId}/status")
    public ResponseEntity<Void> updateHotelStatus(
            @PathVariable Long hotelId,
            @RequestBody Map<String, Boolean> payload) {

        adminHotelService.updateHotelStatus(hotelId, payload.get("active"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve/{hotelId}")
    public ResponseEntity<Void> approveHotel(@PathVariable Long hotelId) {
        adminHotelService.approveHotel(hotelId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{hotelId}")
    public ResponseEntity<Void> rejectHotel(@PathVariable Long hotelId) {
        adminHotelService.deleteHotel(hotelId);
        return ResponseEntity.ok().build();
    }
}