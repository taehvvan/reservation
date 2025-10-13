package com.example.backend.coupon;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUser_IdAndIsUsedFalseOrderByCoupon_ExpiryDateAsc(Integer userId);
    Optional<UserCoupon> findById(Long id);
    void deleteByCouponId(Long couponId);
    List<UserCoupon> findAllByOrderByIssuedAtDesc();
}