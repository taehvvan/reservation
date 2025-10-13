package com.example.backend.detail;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
public class DetailRequest {
    private Long hId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int numberOfRooms;
    private int numberOfPeople;

    @JsonCreator // JSON을 객체로 변환할 때 사용할 생성자 지정
    public DetailRequest(
            @JsonProperty("hId") Long hId, // hId 필드를 명시적으로 매핑
            @JsonProperty("startDate") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @JsonProperty("endDate") @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @JsonProperty("numberOfRooms") int numberOfRooms,
            @JsonProperty("numberOfPeople") int numberOfPeople) {

        // 생성자 내에서 모든 필드를 초기화
        this.hId = hId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfRooms = numberOfRooms;
        this.numberOfPeople = numberOfPeople;
    }
}
