package com.example.backend.manager;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.login.security.jwt.JwtTokenProvider;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelDTO;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // 숙소 기본 정보 + 이미지 저장
    @PostMapping("/save")
    public ResponseEntity<?> saveHotel(
            @RequestPart("hotel") HotelDTO hotelDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        System.out.println("축하합니다 백앤드 입성");
        System.out.println(hotelDto);
        System.out.println(hotelDto.getHName());
        Hotel savedHotel = hotelService.saveHotel(hotelDto, images);
        return ResponseEntity.ok(savedHotel);
    }
}