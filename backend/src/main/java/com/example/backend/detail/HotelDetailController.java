package com.example.backend.detail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.search.HotelDTO;

/**
 * 호텔 검색 API 엔드포인트를 제공하는 컨트롤러 클래스.
 * 클라이언트의 HTTP 요청을 받아 HotelSearchService로 전달합니다.
 */
@RestController
@RequestMapping("/api/detail")
public class HotelDetailController {

    private final HotelDetailService hotelDetailService;

    public HotelDetailController(HotelDetailService hotelDetailService) {
        this.hotelDetailService = hotelDetailService;
    }

    @PostMapping
    public ResponseEntity<HotelDTO> detailHotels(@RequestBody DetailRequest request) {
        System.out.println("===========================" + request);
        HotelDTO hotelDTO = hotelDetailService.detail(request);
        return ResponseEntity.ok(hotelDTO);
    }
}