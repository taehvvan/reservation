package com.example.backend.manager;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.search.Hotel;
import com.example.backend.search.HotelImage;
import com.example.backend.search.Room;
import com.example.backend.search.ServiceEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerHotelDTO {
    private Long id;
    private String name;
    private String location;
    private String type;
    private Integer stars;
    private Double latitude;
    private Double longitude;
    private String status;
    private List<String> amenities;
    private List<String> images;
    private String image;
    private List<ManagerRoomDTO> rooms;
    private List<Long> serviceIds; 

    public static ManagerHotelDTO fromEntity(Hotel hotel, String baseUrl) {

        
        //String baseUrl = "http://localhost:8888"; // ✅ 서버 기본 주소

        // ✅ 호텔의 전체 이미지 리스트 경로 수정
        List<String> fullImageUrls = hotel.getImages().stream()
                .map(image -> baseUrl + image.getImageUrl())
                .collect(Collectors.toList());

        // new ManagerHotelDTO() 대신 builder()를 사용합니다.
        return ManagerHotelDTO.builder()
                .id(hotel.getHId())
                .name(hotel.getHName())
                .location(hotel.getAddress())
                .type(hotel.getType())
                .stars(hotel.getStar())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .status(hotel.getStatus())
                .rooms(hotel.getRooms() != null ?
                   hotel.getRooms().stream()
                        .map(room -> ManagerRoomDTO.fromEntity(room, baseUrl))
                        .collect(Collectors.toList()) :
                   null)
                .images(fullImageUrls) // ✅ 수정된 전체 이미지 URL 리스트 적용
                .image(!fullImageUrls.isEmpty() ? fullImageUrls.get(0) : null)
                .serviceIds(hotel.getServices() != null ? 
                            hotel.getServices().stream()
                                    .map(ServiceEntity::getServiceId)
                                    .collect(Collectors.toList()) : 
                            null)
                
                .build(); // 마지막에 .build()를 호출하여 객체 생성 완료
    }
}