package com.example.backend.reservation;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // Jackson이 유사시 사용할 기본 생성자
public class ReservationRequest {
    private Integer rId;
    private Integer uId;
    private Long hId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkin;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkout;
    
    private int people;
    private int price;
    private int roomCount;

    /**
     * Jackson이 JSON 데이터를 객체로 변환할 때 사용할 생성자임을 명시합니다.
     * @JsonProperty 어노테이션은 JSON의 각 필드 이름을 생성자의 파라미터와 정확히 매핑해줍니다.
     */
    @JsonCreator
    public ReservationRequest(
            @JsonProperty("rId") Integer rId,
            @JsonProperty("uId") Integer uId,
            @JsonProperty("hId") Long hId,
            @JsonProperty("checkin") LocalDate checkin,
            @JsonProperty("checkout") LocalDate checkout,
            @JsonProperty("people") int people,
            @JsonProperty("price") int price,
            @JsonProperty("roomCount") int roomCount) {
        this.rId = rId;
        this.uId = uId;
        this.hId = hId;
        this.checkin = checkin;
        this.checkout = checkout;
        this.people = people;
        this.price = price;
        this.roomCount = roomCount;
    }
}