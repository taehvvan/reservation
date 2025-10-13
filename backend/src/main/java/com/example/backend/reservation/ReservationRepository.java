package com.example.backend.reservation;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.search.Hotel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

       /* 
    @Query("SELECT new com.example.backend.reservation.ReservationResponseDTO(" +
           "r.reId, r.checkin, r.checkout, r.people, r.price, r.status, " +
           "rm.type, " +
           "h.hName, h.address, 'https://placehold.co/300x200?text=Hotel+Image') " +
           "FROM Reservation r " +
           "LEFT JOIN r.room rm " + // <-- INNER JOIN에서 LEFT JOIN으로 변경
           "LEFT JOIN rm.hotel h " + // <-- INNER JOIN에서 LEFT JOIN으로 변경
           "WHERE r.user.id = :userId " +
           "ORDER BY r.checkin DESC")
    List<ReservationResponseDTO> findMyReservationsByUserId(@Param("userId") Integer userId);
       */

    Optional<Reservation> findByReId(Integer reId);


    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId")
    List<Reservation> findRawReservationsByUserIdForDebug(@Param("userId") Integer userId);

    List<Reservation> findByHotel_hIdIn(List<Long> hotelIds);

    List<Reservation> findByUser_IdOrderByReIdDesc(Integer userId);

    Optional<Reservation> findByOrderId(String orderId);

    @Query("SELECT r FROM Reservation r JOIN r.hotel h WHERE h.user.id = :ownerId ORDER BY r.reId DESC")
    List<Reservation> findAllByHotelOwnerId(@Param("ownerId") Integer ownerId);

    List<Reservation> findByRoomHotel(Hotel hotel);

    /**
     * JPQL: createdAt의 날짜 부분만 추출하여 오늘 날짜와 비교
     * @param today 오늘 날짜 (LocalDate)
     * @return 예약 건수
     */
    @Query("SELECT COUNT(r) FROM Reservation r " + 
           "WHERE r.createdAt BETWEEN :startOfDay AND :endOfDay " +
           "AND r.status = '예약 완료'") // 예약 취소가 아닐때 조건 추가
    Long countCompletedReservationsBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    

    //지역 별 총 예약금액
    @Query("SELECT r.hotel.region, SUM(r.price) " +
           "FROM Reservation r " +
           "WHERE r.status = '예약 완료'" + //  예약 취소가 아닐때 조건
           "GROUP BY r.hotel.region " +
           "ORDER BY SUM(r.price) DESC")
    List<Object[]> findTotalSalesByRegion();

    //특정 기간 동안 지역별 총 예약 금액 집계함. startDate랑 endDate는 서비스에서 
     @Query("SELECT r.hotel.region, SUM(r.price) " +
           "FROM Reservation r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.status = '예약 완료'" + // 예약 취소가 아닐때 조건
           "GROUP BY r.hotel.region " +
           "ORDER BY SUM(r.price) DESC")
    List<Object[]> findSalesByRegionForPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    //모든 호텔 년도, 월, 총금액, 예약건수
    @Query("SELECT FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt), SUM(r.price), COUNT(r) " +
           "FROM Reservation r " +
           "WHERE r.status = '예약 완료'" + //예약 취소가 아닐때 조건
           "GROUP BY FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt) " +
           "ORDER BY FUNCTION('YEAR', r.createdAt) ASC, FUNCTION('MONTH', r.createdAt) ASC")
    List<Object[]> findTotalSalesAndCountMonthly();

    //일별 추이
    @Query("SELECT FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt), FUNCTION('DAY', r.createdAt), SUM(r.price), COUNT(r) " +
           "FROM Reservation r " +
           "WHERE r.status = '예약 완료'" + //예약 취소가 아닐때 조건
           "GROUP BY 1, 2, 3 " +
           "ORDER BY 1 ASC, 2 ASC, 3 ASC")
    List<Object[]> findTotalSalesAndCountDaily();

    // 연도별 추이
    @Query("SELECT FUNCTION('YEAR', r.createdAt), SUM(r.price), COUNT(r) " +
           "FROM Reservation r " +
           "WHERE r.status = '예약 완료'" + //예약 취소가 아닐때 조건 
           "GROUP BY 1 " +
           "ORDER BY 1 ASC")
    List<Object[]> findTotalSalesAndCountYearly();

    @Query("SELECT FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt), FUNCTION('DAY', r.createdAt), SUM(r.price), COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate " + // 🎯 기간 필터링 추가
            "AND r.status = '예약 완료'" +
            "GROUP BY 1, 2, 3 " +
            "ORDER BY 1 ASC, 2 ASC, 3 ASC")
    List<Object[]> findTotalSalesAndCountDailyForPeriod(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt), SUM(r.price), COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate " + // 🎯 기간 필터링 추가
            "AND r.status = '예약 완료'" +
            "GROUP BY FUNCTION('YEAR', r.createdAt), FUNCTION('MONTH', r.createdAt) " +
            "ORDER BY FUNCTION('YEAR', r.createdAt) ASC, FUNCTION('MONTH', r.createdAt) ASC")
    List<Object[]> findTotalSalesAndCountMonthlyForPeriod(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

            

          @Query("SELECT FUNCTION('YEAR', r.createdAt), SUM(r.price), COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate " + // 🎯 기간 필터링 추가
            "AND r.status = '예약 완료'" + 
            "GROUP BY 1 " +
            "ORDER BY 1 ASC")
    List<Object[]> findTotalSalesAndCountYearlyForPeriod(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);  


}