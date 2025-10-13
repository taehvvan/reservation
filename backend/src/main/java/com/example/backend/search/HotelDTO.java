package com.example.backend.search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.stream.Collectors;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelDTO {

    @JsonProperty("hId")
    private Long hId;

    @JsonProperty("hName")
    private String hName;

    private String type;
    private String region;
    private String address;
    private Integer star;
    private boolean active;
    private String info;
    private Double latitude;
    private Double longitude;
    private Integer imageCount;
    private String status;
    private String image;

    private List<RoomDTO> rooms;
    private List<ReviewDTO> reviews;
    private List<ServiceDTO> services; // <-- DTO로 변경
    private List<HotelImageDTO> images;

    private Double avgScore; // 평균 점수
    private Integer reviewCount; // 리뷰 개수

    private Integer minPrice; // <-- 최저가 추가

    private List<Long> serviceIds;

    // 기본 생성자
    public HotelDTO() {
    }

    // ✅ Hotel 엔티티를 DTO로 변환하는 생성자
    public HotelDTO(Hotel hotel) {
        this.hId = hotel.getHId();
        this.hName = hotel.getHName();
        this.type = hotel.getType();
        this.region = hotel.getRegion();
        this.address = hotel.getAddress();
        this.star = hotel.getStar();
        this.active = hotel.isActive();
        this.info = hotel.getInfo();
        this.latitude = hotel.getLatitude();
        this.longitude = hotel.getLongitude();
        this.status = hotel.getStatus();

        // 연관된 엔티티 리스트들을 각각 DTO 리스트로 변환 (null 체크 포함)
        this.rooms = (hotel.getRooms() != null)
                ? hotel.getRooms().stream().map(RoomDTO::new).collect(Collectors.toList())
                : Collections.emptyList();

        this.reviews = (hotel.getReviews() != null)
                ? hotel.getReviews().stream().map(ReviewDTO::new).collect(Collectors.toList())
                : Collections.emptyList();

        this.services = (hotel.getServices() != null)
                ? hotel.getServices().stream().map(ServiceDTO::new).collect(Collectors.toList())
                : Collections.emptyList();

        this.images = (hotel.getImages() != null)
                ? hotel.getImages().stream().map(HotelImageDTO::new).collect(Collectors.toList())
                : Collections.emptyList();

        // 리뷰 정보를 기반으로 리뷰 개수와 평균 점수 계산
        if (this.reviews != null && !this.reviews.isEmpty()) {
            this.reviewCount = this.reviews.size();
            this.avgScore = this.reviews.stream()
                    .mapToDouble(ReviewDTO::getScore) // ReviewDTO에 getScore() 메서드 필요
                    .average()
                    .orElse(0.0);
        } else {
            this.reviewCount = 0;
            this.avgScore = 0.0;
        }

        // 객실 정보를 기반으로 최저가 계산
        if (this.rooms != null && !this.rooms.isEmpty()) {
            this.minPrice = this.rooms.stream()
                    .mapToInt(RoomDTO::getPrice) // RoomDTO에 getPrice() 메서드 필요
                    .min()
                    .orElse(0);
        } else {
            this.minPrice = 0;
        }
    }
}
