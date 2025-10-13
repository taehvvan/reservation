package com.example.backend.search;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "r_id")
    private Integer rId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "h_id", nullable = false)
    private Hotel hotel;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "info")
    private String info;

    @Column(name = "people", nullable = false)
    private Integer people;

    @Column(name = "checkin_time")
    private String checkinTime;

    @Column(name = "checkout_time")
    private String checkoutTime;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HotelImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomAvailability> roomAvailabilities = new ArrayList<>();
}