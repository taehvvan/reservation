package com.example.backend.manager;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final HotelRepository hotelRepository;

    // 로그인한 매니저가 관리하는 호텔 정보를 반환하는 API
    @GetMapping("/hotel-info")
    public ResponseEntity<Map<String, Object>> getManagedHotelInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        UserEntity manager = principalDetails.getUser();

        // 매니저의 companyName과 일치하는 호텔을 찾습니다.
        Hotel managedHotel = hotelRepository.findByhName(manager.getCompanyName())
                .orElse(null);

        if (managedHotel == null) {
            // 호텔을 찾을 수 없는 경우
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "관리하는 호텔 정보를 찾을 수 없습니다."));
        }

        // 호텔 ID와 이름을 포함한 맵을 생성하여 반환
        Map<String, Object> hotelInfo = Map.of(
                "hotelId", managedHotel.getHId(),
                "hotelName", managedHotel.getHName()
        );

        return ResponseEntity.ok(hotelInfo);
    }
}