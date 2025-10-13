package com.example.backend.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 호텔 검색 API 엔드포인트를 제공하는 컨트롤러 클래스.
 * 클라이언트의 HTTP 요청을 받아 HotelSearchService로 전달합니다.
 */
@RestController
@RequestMapping("/api/search")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    public HotelSearchController(HotelSearchService hotelSearchService) {
        this.hotelSearchService = hotelSearchService;
    }

    /**
     * 클라이언트로부터 검색 요청을 처리합니다.
     *
     * @param request 검색 요청 정보 (JSON 형식)
     * @return 검색 결과 호텔 목록 (DTO) 또는 결과가 없을 경우 (204 No Content)
     */
    @PostMapping
    public ResponseEntity<List<HotelDTO>> searchHotels(@RequestBody SearchRequest request) {
        List<HotelDTO> hotels = hotelSearchService.search(request);
        if (hotels.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(hotels);
    }
}