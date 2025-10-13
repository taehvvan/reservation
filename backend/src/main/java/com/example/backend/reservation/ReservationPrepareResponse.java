package com.example.backend.reservation;

import com.example.backend.search.HotelDTO;
import com.example.backend.search.RoomDTO;
import com.example.backend.search.Hotel;
import com.example.backend.search.Room;
import lombok.Getter;

// Lombok 어노테이션을 사용하여 간단하게 구현
@Getter
public class ReservationPrepareResponse {

    private final String orderId;
    private final Integer reservationId;
    private final String hotelName;
    private final String type;
    private final int amount;

    public ReservationPrepareResponse(Reservation reservation) {
        this.orderId = reservation.getOrderId();
        this.reservationId = reservation.getReId();
        this.hotelName = reservation.getHotel().getHName();
        this.type = reservation.getRoom().getType();
        this.amount = reservation.getPrice();
    }
}