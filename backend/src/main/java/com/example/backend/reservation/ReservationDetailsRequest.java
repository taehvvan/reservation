package com.example.backend.reservation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReservationDetailsRequest {
    private String phone;
    private Integer uId; // 프론트엔드에서 u_id 대신 uId로 보내므로 필드명 일치
}