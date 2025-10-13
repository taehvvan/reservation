package com.example.backend.search;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "hotel_image")
public class HotelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename")
    private String filename;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // ✅ [추가] 이미지 타입 구분 필드 (예: "main", "sub", "room")
    @Column(name = "image_type")
    private String imageType;

    // 호텔과의 관계 (호텔 대표/추가 이미지용)
    @ManyToOne(fetch = FetchType.LAZY)
    // ✅ [수정] h_id는 nullable=true로 변경 (객실 이미지의 경우 이 값은 null이 됩니다)
    @JoinColumn(name = "h_id")
    private Hotel hotel;

    // ✅ [추가] 객실과의 관계 (객실 이미지용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "r_id") // Room 테이블의 FK
    private Room room;

    // 연관관계 편의 메서드 (기존과 동일)
    public void linkHotel(Hotel hotel) {
        this.hotel = hotel;
        if (hotel != null && !hotel.getImages().contains(this)) {
            hotel.getImages().add(this);
        }
    }
}