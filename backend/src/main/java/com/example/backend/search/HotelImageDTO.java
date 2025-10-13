package com.example.backend.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelImageDTO {
    private String imageUrl;
    private String filename;

    // 기본 생성자
    public HotelImageDTO() {}

    // ✅ HotelImage 엔티티를 DTO로 변환하는 생성자
    public HotelImageDTO(HotelImage hotelImage) {
        this.imageUrl = hotelImage.getImageUrl(); // 엔티티의 이미지 URL 필드
        this.filename = hotelImage.getFilename(); // 엔티티의 파일 이름 필드
    }
}