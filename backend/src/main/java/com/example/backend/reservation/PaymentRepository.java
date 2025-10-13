package com.example.backend.reservation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // 결제번호 + 전화번호로 조회
    Optional<Payment> findByReservation_OrderIdAndPhone(String orderId, String phone);

    @Transactional
    void deleteByReservation_ReId(Integer reservationId);

    Optional<Payment> findByReservation(Reservation reservation);

    Optional<Payment> findBypIdAndPhone(Integer pId, String phone);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "JOIN p.reservation r " + // Payment 엔티티의 reservation 필드를 통해 Reservation 엔티티와 조인
           "WHERE FUNCTION('YEAR', p.payDate) = :year " +
           "AND FUNCTION('MONTH', p.payDate) = :month " +
           "AND r.status = '예약 완료'") // 예약 상태 확인해서 데이터 가져옴
    Long findTotalAmountByYearAndMonth(@Param("year") int year, @Param("month") int month);

    //총 매출
    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "JOIN p.reservation r " + // Payment와 Reservation 조인
           "WHERE r.status = '예약 완료'") // 예약 취소 아닌 건만 필터링
    Long findTotalPlatformRevenue();

    Optional<Payment> findByReservationReId(Integer reId);
    
}
