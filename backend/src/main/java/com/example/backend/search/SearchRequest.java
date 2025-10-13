package com.example.backend.search;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * 호텔 검색 요청을 위한 DTO (Data Transfer Object) 클래스.
 * 클라이언트로부터 검색 조건을 받아옵니다.
 */
@Getter
@Setter
public class SearchRequest {
    private String region;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfRooms;
    private int numberOfPeople;
}
