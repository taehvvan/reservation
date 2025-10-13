package com.example.backend.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    // 새로운 쿠폰 타입 생성 API
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        Coupon.CouponType type = Coupon.CouponType.valueOf((String) payload.get("type"));
        int value = (Integer) payload.get("value");
        LocalDate expiryDate = LocalDate.parse((String) payload.get("expiryDate"));

        Coupon newCoupon = couponService.createCoupon(name, type, value, expiryDate);
        return ResponseEntity.ok(newCoupon);
    }

    // 특정 유저에게 쿠폰 발급 API
    @PostMapping("/issue")
    public ResponseEntity<UserCoupon> issueCoupon(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        Long couponId = Long.valueOf(payload.get("couponId").toString());
        UserCoupon issuedCoupon = couponService.issueCouponToUser(userId, couponId);
        return ResponseEntity.ok(issuedCoupon);
    }

      @GetMapping
    public ResponseEntity<List<CouponListDTO>> getAllCouponTypes() {
        List<CouponListDTO> coupons = couponService.getAllCouponTypes();
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/issue-all")
    public ResponseEntity<Void> issueCouponToAll(@RequestBody Map<String, Long> payload) {
        couponService.issueCouponToAllUsers(payload.get("couponId"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCouponType(@PathVariable Long couponId) {
        couponService.deleteCouponType(couponId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/issued/{userCouponId}")
    public ResponseEntity<Void> deleteIssuedCoupon(@PathVariable Long userCouponId) {
        couponService.deleteIssuedCoupon(userCouponId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/issued")
    public ResponseEntity<List<IssuedCouponDTO>> getIssuedCoupons() {
        List<IssuedCouponDTO> issuedCoupons = couponService.getAllIssuedCoupons();
        return ResponseEntity.ok(issuedCoupons);
    }
}