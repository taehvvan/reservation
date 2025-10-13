package com.example.backend.search;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByHotel(Hotel hotel);
}