package com.example.backend.admin;

import com.fasterxml.jackson.annotation.JsonProperty; // [추가] import
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminHotelDto {

    @JsonProperty("hId") // [추가] JSON 필드명을 'hId'로 강제
    private Long hId;

    @JsonProperty("hName") // [추가] JSON 필드명을 'hName'으로 강제
    private String hName;

    private String type;
    private String region;
    private boolean active;
    private String status;
    private Long monthlyRevenue;
    private Long bookingCount;
    private Double averageRating;
}