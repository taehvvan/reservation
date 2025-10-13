package com.example.backend.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelPicRepository extends JpaRepository<HotelPic, Long> {
    List<HotelPic> findByHotel_hId(Long hId); // ← 대소문자 정확히 hId 로
}