package com.example.backend.manager;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelRoomDTO {

    private Long hId;
    private String hName;
    private String type;
    private String region;
    private String address;
    private int star;
    // [추가] RoomSaveRequestDTO 리스트를 받을 필드
    private List<RoomSaveRequestDTO> rooms;
    
}
