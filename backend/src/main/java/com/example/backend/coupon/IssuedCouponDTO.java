package com.example.backend.coupon;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IssuedCouponDTO {
    private final Long userCouponId;
    private final String userName;
    private final String userEmail;
    private final String couponName;
    private final LocalDateTime issuedAt;

    public IssuedCouponDTO(UserCoupon userCoupon) {
        this.userCouponId = userCoupon.getId();
        this.userName = userCoupon.getUser().getName();
        this.userEmail = userCoupon.getUser().getEmail();
        this.couponName = userCoupon.getCoupon().getName();
        this.issuedAt = userCoupon.getIssuedAt();
    }
}