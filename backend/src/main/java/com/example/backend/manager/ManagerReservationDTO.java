package com.example.backend.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerReservationDTO {
    private final Integer reservationId;
    private final String orderId;
    private final Integer userId;      // 회원 ID (비회원은 null)
    private final String guestName;    // 예약자 이름 (회원/비회원 모두)
    private final String phone;        // 예약자 연락처
    private final String hotelName;
    private final String roomType;
    private final String checkIn;
    private final String checkOut;
    private final String status;
    private String hotelImage;
    private String address;
}