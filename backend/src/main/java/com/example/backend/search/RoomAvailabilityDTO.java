package com.example.backend.search;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomAvailabilityDTO {
    private LocalDate date;
    private int availableCount;

    // ✅ RoomAvailability 엔티티를 DTO로 변환하는 생성자
    public RoomAvailabilityDTO(RoomAvailability availability) {
        this.date = availability.getDate(); // 엔티티의 날짜 필드
        this.availableCount = availability.getAvailableCount(); // 엔티티의 사용 가능 객실 수 필드
    }
}
