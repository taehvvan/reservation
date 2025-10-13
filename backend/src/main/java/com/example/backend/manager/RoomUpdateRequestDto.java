package com.example.backend.manager;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomUpdateRequestDto {
    private String type;
    private Integer price;
    private Integer count;
    private Integer people;
    private String checkinTime;
    private String checkoutTime;
    private String image;
}
