package com.example.backend.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.admin.AdminHotelDto;
import com.example.backend.register.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

        @Query(value = """
                        SELECT DISTINCT h.*
                        FROM hotel h
                        JOIN room r ON h.h_id = r.h_id
                        WHERE h.region LIKE CONCAT('%', :region, '%')
                          AND h.active = true
                          AND h.status = '승인'
                          AND r.count >= :numberOfRooms
                          AND (r.people * :numberOfRooms) >= :numberOfPeople
                          AND NOT EXISTS (
                              SELECT 1
                              FROM room_availability ra
                              WHERE ra.r_id = r.r_id
                                AND ra.date BETWEEN :startDate AND :endDate
                                AND ra.available_count < :numberOfRooms
                          )
                        """, nativeQuery = true)
        List<Hotel> searchHotels(
                        @Param("region") String region,
                        @Param("numberOfPeople") int numberOfPeople,
                        @Param("numberOfRooms") int numberOfRooms,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("""
                        SELECT MIN(r.price)
                        FROM Room r
                        WHERE r.hotel.hId = :hotelId
                          AND r.count >= :numberOfRooms
                          AND (r.people * :numberOfRooms) >= :numberOfPeople
                          AND NOT EXISTS (
                            SELECT 1
                            FROM RoomAvailability ra
                            WHERE ra.room = r
                              AND ra.date BETWEEN :startDate AND :endDate
                              AND ra.availableCount < :numberOfRooms
                          )
                        """)
        Integer findMinPriceByHotelAndConditions(
                        @Param("hotelId") Long hotelId,
                        @Param("numberOfRooms") int numberOfRooms,
                        @Param("numberOfPeople") int numberOfPeople,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // [최종 수정] new AdminHotelDto의 파라미터 순서를 DTO 필드 순서와 정확히 일치시킴
        @Query("SELECT new com.example.backend.admin.AdminHotelDto(" +
                        "h.hId, h.hName, h.type, h.region, h.active, h.status, " +
                        "COALESCE((SELECT SUM(res.price) FROM Reservation res WHERE res.room.hotel = h AND res.createdAt >= :monthAgo), 0L), "
                        +
                        "COALESCE((SELECT COUNT(res) FROM Reservation res WHERE res.room.hotel = h AND res.createdAt >= :monthAgo), 0L), "
                        +
                        "COALESCE((SELECT AVG(rev.score) FROM Review rev WHERE rev.hotel = h), 0.0)) " +
                        "FROM Hotel h WHERE h.type = :type")
        List<AdminHotelDto> findHotelsForAdminByType(@Param("type") String type,
                        @Param("monthAgo") LocalDateTime monthAgo);

        // [최종 수정] new AdminHotelDto의 파라미터 순서를 DTO 필드 순서와 정확히 일치시킴
        @Query("SELECT new com.example.backend.admin.AdminHotelDto(" +
                        "h.hId, h.hName, h.type, h.region, h.active, h.status, " +
                        "COALESCE((SELECT SUM(res.price) FROM Reservation res WHERE res.room.hotel = h AND res.createdAt >= :monthAgo), 0L), "
                        +
                        "COALESCE((SELECT COUNT(res) FROM Reservation res WHERE res.room.hotel = h AND res.createdAt >= :monthAgo), 0L), "
                        +
                        "COALESCE((SELECT AVG(rev.score) FROM Review rev WHERE rev.hotel = h), 0.0)) " +
                        "FROM Hotel h")
        List<AdminHotelDto> findAllForAdmin(@Param("monthAgo") LocalDateTime monthAgo);

        Optional<Hotel> findByhNameAndAddress(String hName, String address);

        List<Hotel> findByUser_Id(Integer id);

        Long countByStatus(String status);

        Optional<Hotel> findByUser(UserEntity user);

        Optional<Hotel> findByhName(String hName);

        List<Hotel> findAllByUser(UserEntity user);
}