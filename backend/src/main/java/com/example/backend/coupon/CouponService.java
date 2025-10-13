package com.example.backend.coupon;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Logger import
import org.slf4j.LoggerFactory; // LoggerFactory import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class); // Logger 추가

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    // 관리자: 새로운 종류의 쿠폰 생성
    public Coupon createCoupon(String name, Coupon.CouponType type, int value, LocalDate expiryDate) {
        log.info("새로운 쿠폰 생성 시도: {}", name); // 로그 추가
        Coupon coupon = new Coupon();
        coupon.setName(name);
        coupon.setType(type);
        coupon.setDiscountValue(value);
        coupon.setExpiryDate(expiryDate);
        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("쿠폰 생성 완료: ID {}", savedCoupon.getId()); // 로그 추가
        return savedCoupon;
    }

    // 관리자: 특정 유저에게 쿠폰 발급
    public UserCoupon issueCouponToUser(Integer userId, Long couponId) {
        log.info("사용자 ID: {} 에게 쿠폰 ID: {} 발급 시도", userId, couponId); // 로그 추가
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);
        log.info("쿠폰 발급 완료: UserCoupon ID {}", savedUserCoupon.getId()); // 로그 추가
        return savedUserCoupon;
    }

    // 회원가입 시 웰컴 쿠폰 자동 발급
    public void issueWelcomeCoupon(UserEntity user) {
        log.info("신규 회원 쿠폰 발급 시도: {}", user.getEmail()); // 로그 추가
        // "신규 회원 10% 할인 쿠폰"이 없으면 생성
        Coupon welcomeCoupon = couponRepository.findByName("신규 회원 10% 할인 쿠폰")
                .orElseGet(() -> createCoupon(
                        "신규 회원 10% 할인 쿠폰",
                        Coupon.CouponType.PERCENT,
                        10,
                        LocalDate.now().plusMonths(1) // 유효기간 1달
                ));

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(welcomeCoupon);
        userCouponRepository.save(userCoupon);
        log.info("{} 에게 신규 회원 쿠폰 발급 완료", user.getEmail()); // 로그 추가
    }

    // 사용자 쿠폰 목록 조회
    @Transactional(readOnly = true)
    public List<UserCoupon> getAvailableUserCoupons(Integer userId) {
        return userCouponRepository.findByUser_IdAndIsUsedFalseOrderByCoupon_ExpiryDateAsc(userId);
    }
     @Transactional(readOnly = true)
    public List<CouponListDTO> getAllCouponTypes() {
        return couponRepository.findAll().stream()
                .map(CouponListDTO::new)
                .collect(Collectors.toList());
    }

    // (이전 답변에서 추가했던 DTO 변환 메서드)
    @Transactional(readOnly = true)
    public List<CouponDTO> getAvailableUserCouponsAsDTO(Integer userId) {
        return userCouponRepository.findByUser_IdAndIsUsedFalseOrderByCoupon_ExpiryDateAsc(userId)
                .stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());
    }

    public void useCoupon(Long userCouponId) {
        if (userCouponId == null) return;
        userCouponRepository.findById(userCouponId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
        userCouponRepository.deleteById(userCouponId);
    }

    public void issueCouponToAllUsers(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        List<UserEntity> allUsers = userRepository.findAllByRole("ROLE_USER");
        for (UserEntity user : allUsers) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUser(user);
            userCoupon.setCoupon(coupon);
            userCouponRepository.save(userCoupon);
        }
    }

    public void deleteCouponType(Long couponId) {
        userCouponRepository.deleteByCouponId(couponId);
        couponRepository.deleteById(couponId);
    }

    public void deleteIssuedCoupon(Long userCouponId) {
        userCouponRepository.deleteById(userCouponId);
    }
    
    public void deleteUserCoupon(Long userCouponId) {
        userCouponRepository.deleteById(userCouponId);
    }

    public List<IssuedCouponDTO> getAllIssuedCoupons() {
        return userCouponRepository.findAllByOrderByIssuedAtDesc().stream()
            .map(IssuedCouponDTO::new)
            .collect(Collectors.toList());
    }
}