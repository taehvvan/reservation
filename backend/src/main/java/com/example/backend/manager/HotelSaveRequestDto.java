package com.example.backend.manager;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class HotelSaveRequestDto {
    private Long hId;
    private String name;
    private String location;
    private String type;
    private Integer stars;
    private Double latitude;
    private Double longitude;
    private String checkInTime;
    private String checkOutTime;
    private List<String> amenities;
    private List<RoomSaveRequestDTO> rooms;
    private List<Long> serviceIds;
    
}
