package com.example.backend.manager;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.example.backend.search.Room;

@Getter
@Setter
public class HotelUpdateRequestDto {
    // 기본 정보
    private String name;
    private String location;
    private Integer stars;
    private Double latitude;
    private Double longitude;

    // 서비스 정보 (ID 목록)
    private List<Long> serviceIds;

    // 객실 정보 (객실 DTO 리스트)
    private List<RoomUpdateRequestDto> rooms;
}
