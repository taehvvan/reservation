package com.example.backend.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/search/images")
public class HotelImageController {

    private final HotelImageService hotelImageService;

    @Autowired
    public HotelImageController(HotelImageService hotelImageService) {
        this.hotelImageService = hotelImageService;
    }

    @PostMapping("/hotel/{hotelId}")
    public String uploadHotelImage(@PathVariable Long hotelId, 
                                   @RequestParam("file") MultipartFile file) {
        try {
            hotelImageService.saveHotelImage(hotelId, file);
            return "이미지 저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "이미지 저장 실패";
        }
    }
}
