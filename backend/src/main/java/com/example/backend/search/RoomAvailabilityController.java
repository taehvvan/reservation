package com.example.backend.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    // 예약 가능 여부만 체크 (boolean)
    @GetMapping("/{rId}/availability-check")
    public boolean checkAvailability(
            @PathVariable Integer rId,
            @RequestParam String checkin,
            @RequestParam String checkout,
            @RequestParam int quantity
    ) {
        LocalDate in = LocalDate.parse(checkin);
        LocalDate out = LocalDate.parse(checkout);
        return roomAvailabilityService.isRoomAvailable(rId, in, out, quantity);
    }

    // 날짜별 객실 수 리스트 조회 (프론트에서 availabilities 저장용)
    @GetMapping("/{rId}/availabilities")
    public List<RoomAvailabilityDTO> getAvailabilities(
            @PathVariable Integer rId,
            @RequestParam LocalDate checkin,
            @RequestParam LocalDate checkout
    ) {
        return roomAvailabilityService.getAvailabilities(rId, checkin, checkout);
    }

    // 예약 확정 (객실 차감)
    @PostMapping("/{rId}/reserve")
    public String reserveRoom(
            @PathVariable Integer rId,
            @RequestParam String checkin,
            @RequestParam String checkout,
            @RequestParam int quantity
    ) {
        LocalDate in = LocalDate.parse(checkin);
        LocalDate out = LocalDate.parse(checkout);
        roomAvailabilityService.reserveRoom(rId, in, out, quantity);
        return "예약 성공";
    }
}
