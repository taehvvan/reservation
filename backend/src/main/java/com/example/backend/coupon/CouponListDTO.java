package com.example.backend.coupon;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CouponListDTO {
    private final Long id;
    private final String name;
    private final Coupon.CouponType type;
    private final int discountValue;
    private final LocalDate expiryDate;

    public CouponListDTO(Coupon coupon) {
        this.id = coupon.getId();
        this.name = coupon.getName();
        this.type = coupon.getType();
        this.discountValue = coupon.getDiscountValue();
        this.expiryDate = coupon.getExpiryDate();
    }
}