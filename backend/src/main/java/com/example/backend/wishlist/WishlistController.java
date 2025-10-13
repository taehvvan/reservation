package com.example.backend.wishlist;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.search.HotelDTO;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * 로그인한 사용자의 찜 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<HotelDTO>> getUserWishlist(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) LocalDate checkIn,
            @RequestParam(required = false) LocalDate checkOut,
            @RequestParam(required = false) Integer rooms,
            @RequestParam(required = false) Integer persons) {
        if (principalDetails == null) {
            System.out.println("principalDetails 비어있음");
            return ResponseEntity.status(401).build(); // 인증 안됨
        }
        String email = principalDetails.getUser().getEmail();

        // WishlistService의 필터링 메서드 호출
        List<HotelDTO> wishlist = wishlistService.getWishlistByUserIdWithFilter(email, checkIn, checkOut, rooms, persons);
        
        return ResponseEntity.ok(wishlist);
    }

    /**
     * 찜 목록에 호텔 추가
     */
    @PostMapping("/{hid}")
    public ResponseEntity<Void> addToWishlist(
            @PathVariable Long hid,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principalDetails.getUser().getEmail();

        try {
            wishlistService.addByEmailAndHotelId(email, hid);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 찜 목록에서 호텔 제거
     */
    @DeleteMapping("/{hid}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long hid,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        if (principalDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principalDetails.getUser().getEmail();

        try {
            wishlistService.removeByEmailAndHotelId(email, hid);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
