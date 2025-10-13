package com.example.backend.coupon;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 예: "신규회원 10% 할인 쿠폰"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type; // 할인 타입 (정액, 정률)

    @Column(nullable = false)
    private int discountValue; // 할인 값 (10000, 10)

    @Column(nullable = false)
    private LocalDate expiryDate; // 만료일

    public enum CouponType {
        PERCENT, // 정률 할인
        FIXED    // 정액 할인
    }
}