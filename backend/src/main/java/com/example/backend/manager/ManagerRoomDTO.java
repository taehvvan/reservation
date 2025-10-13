package com.example.backend.manager;

import com.example.backend.search.Room;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerRoomDTO {
    private Integer rId;
    private String type;
    private Integer price;
    private Integer count;
    private Integer people;
    private String checkinTime;
    private String checkoutTime;
    
    // ✅ List<String> images -> String image 로 변경
    private String image;

    public static ManagerRoomDTO fromEntity(Room room, String baseUrl) {
        String fullImageUrl = null;

        if (room.getImages() != null && !room.getImages().isEmpty()) {
            fullImageUrl = baseUrl + room.getImages().get(0).getImageUrl();
        }

        ManagerRoomDTO dto = new ManagerRoomDTO();
        dto.setRId(room.getRId());
        dto.setType(room.getType());
        dto.setPrice(room.getPrice());
        dto.setCount(room.getCount());
        dto.setPeople(room.getPeople());
        dto.setCheckinTime(room.getCheckinTime());
        dto.setCheckoutTime(room.getCheckoutTime());
        dto.setImage(fullImageUrl);
        
        
        return dto;
    }

    
}
