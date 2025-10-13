package com.example.backend.coupon;

import com.example.backend.login.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/my-coupons")
    public ResponseEntity<List<CouponDTO>> getMyCoupons(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = principalDetails.getUser().getId();
        List<CouponDTO> coupons = couponService.getAvailableUserCouponsAsDTO(userId);
        return ResponseEntity.ok(coupons);
    }

    @DeleteMapping("/my-coupons/{userCouponId}")
    public ResponseEntity<Void> deleteMyCoupon(@PathVariable Long userCouponId) {
        couponService.deleteUserCoupon(userCouponId);
        return ResponseEntity.ok().build();
    }
}