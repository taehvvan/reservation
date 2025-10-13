package com.example.backend.manager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomSaveRequestDTO {
    // ★★★★★ 'name'을 'type'으로 변경 ★★★★★
    private String type; // 객실 종류
    private Integer price;
    private Integer count; // 총 객실 수
    private Integer people; // 최대 인원
    private String checkinTime;
    private String checkoutTime;
     private String image;
}