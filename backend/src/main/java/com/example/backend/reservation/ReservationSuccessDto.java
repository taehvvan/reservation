package com.example.backend.reservation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReservationSuccessDto {
    private String orderId;
    private String hotelName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int price;

    public static ReservationSuccessDto fromEntity(Reservation reservation) {
        return ReservationSuccessDto.builder()
                .orderId(reservation.getOrderId())
                .hotelName(reservation.getHotel().getHName())
                .checkIn(reservation.getCheckin())
                .checkOut(reservation.getCheckout())
                .price(reservation.getPrice())
                .build();
    }
}