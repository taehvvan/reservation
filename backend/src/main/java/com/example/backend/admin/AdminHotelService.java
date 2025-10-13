package com.example.backend.admin;

import com.example.backend.search.Hotel;
import com.example.backend.search.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자의 호텔 관리 기능에 대한 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHotelService {

    private final HotelRepository hotelRepository;

    public List<AdminHotelDto> getHotels(String type) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        // [수정] "ALL" 타입일 경우와 아닐 경우를 분기하여 처리
        if ("ALL".equalsIgnoreCase(type)) {
            // 타입이 "ALL"이면, 새로 추가한 findAllForAdmin 메소드를 호출합니다.
            return hotelRepository.findAllForAdmin(oneMonthAgo);
        } else {
            // 특정 타입이 주어지면, 기존 메소드를 그대로 사용합니다.
            return hotelRepository.findHotelsForAdminByType(type, oneMonthAgo);
        }
    }

    @Transactional
    public void updateHotelStatus(Long hotelId, boolean active) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 호텔입니다. ID: " + hotelId));

        hotel.setActive(active);
        hotelRepository.save(hotel);
    }

    @Transactional
    public void approveHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 호텔이 존재하지 않습니다. id=" + hotelId));

        hotel.setStatus("승인"); // 상태를 "승인"으로 변경
        hotelRepository.save(hotel); // 변경된 값 저장
    }

    @Transactional
    public void deleteHotel(Long hotelId) {
        hotelRepository.deleteById(hotelId);
    }
}