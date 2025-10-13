package com.example.backend.coupon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CouponDTO {
    private final Long userCouponId;
    private final String name;
    private final Coupon.CouponType type;
    private final int discount; // discountValue -> discount
    private final LocalDate expiryDate;
    private final LocalDateTime issuedAt;
    
    @JsonProperty("isExpired")
    private final boolean isExpired;

    public CouponDTO(UserCoupon userCoupon) {
        this.userCouponId = userCoupon.getId();
        this.name = userCoupon.getCoupon().getName();
        this.type = userCoupon.getCoupon().getType();
        this.discount = userCoupon.getCoupon().getDiscountValue();
        this.expiryDate = userCoupon.getCoupon().getExpiryDate();
        this.issuedAt = userCoupon.getIssuedAt();
        this.isExpired = LocalDate.now().isAfter(userCoupon.getCoupon().getExpiryDate());
    }
}