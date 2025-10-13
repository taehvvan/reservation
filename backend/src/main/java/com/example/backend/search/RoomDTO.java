package com.example.backend.search;

import com.example.backend.search.RoomAvailability;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class RoomDTO {

    @JsonProperty("rId")
    private Integer rId;
    
    private String type;
    private int count;
    private int people;
    private int price;
    private String info;
    private String checkinTime;
    private String checkoutTime;
    private String image;
    private Long hId;
    // ✅ DTO에 필드를 명확하게 선언합니다.
    private List<RoomAvailabilityDTO> availabilities;

    public RoomDTO(Room room) {
        this.rId = room.getRId();
        this.type = room.getType();
        this.count = room.getCount();
        this.people = room.getPeople();
        this.price = room.getPrice();
        this.info = room.getInfo();
        this.checkinTime = room.getCheckinTime();
        this.checkoutTime = room.getCheckoutTime();

        if (room.getHotel() != null) {
            this.hId = room.getHotel().getHId();
        }

        // ✅ [수정] 'room.getAvailabilities()'를 실제 Room 엔티티의 getter 메소드 이름으로 변경해야 합니다.
        // 예: room.getRoomAvailabilities()
        if (room.getRoomAvailabilities() != null) {
            this.availabilities = room.getRoomAvailabilities().stream()
                                      .map(RoomAvailabilityDTO::new)
                                      .collect(Collectors.toList());
        } else {
            this.availabilities = Collections.emptyList();
        }
    }
}
