package com.example.backend.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    /**
     * 특정 객실(rId)과 날짜(date)에 대한 RoomAvailability 조회
     */
    Optional<RoomAvailability> findByRoom_rIdAndDate(Integer rId, LocalDate date);

    // 특정 방의 모든 날짜 재고를 찾는 메서드
    List<RoomAvailability> findByRoom_rId(Integer rId);

    // 특정 방의 시작일과 종료일 사이의 모든 재고 정보를 조회하는 메서드
    List<RoomAvailability> findByRoom_rIdAndDateBetween(Integer rId, LocalDate startDate, LocalDate endDate);

}