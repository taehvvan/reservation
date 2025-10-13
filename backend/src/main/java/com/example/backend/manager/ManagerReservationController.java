package com.example.backend.manager;

import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager") // 관리자 전용 API 경로
@RequiredArgsConstructor
public class ManagerReservationController {

    private final ManagerReservationService managerReservationService;
    private final ManagerHotelService managerHotelService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ManagerReservationDTO>> getManagerReservations(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        // 현재 로그인한 매니저(owner)의 ID를 가져옵니다.
        Integer ownerId = principalDetails.getUser().getId(); 
        
        List<ManagerReservationDTO> reservations = managerReservationService.getReservationsForManager(ownerId);
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<Map<String, String>> cancelReservation(
            @PathVariable Integer reservationId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) throws AccessDeniedException {
        
        Integer ownerId = principalDetails.getUser().getId();
        managerReservationService.cancelReservationByManager(reservationId, ownerId);
        
        return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 취소되었습니다."));
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<ManagerHotelDTO>> getManagerHotels(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        Integer managerId = principalDetails.getUser().getId();
        List<ManagerHotelDTO> hotels = managerHotelService.getHotelsByManager(managerId);
        return ResponseEntity.ok(hotels);
    }

    @PostMapping(value = "/hotels", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ManagerHotelDTO> createHotel(
            @RequestPart("hotelDto") HotelSaveRequestDto hotelDto,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        UserEntity manager = principalDetails.getUser();
        ManagerHotelDTO createdHotel = managerHotelService.createHotel(hotelDto, images, manager);
        return ResponseEntity.ok(createdHotel);
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<ManagerHotelDTO> getHotelById(
            @PathVariable Long hotelId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        UserEntity manager = principalDetails.getUser();
        ManagerHotelDTO hotelDetails = managerHotelService.getHotelDetails(hotelId, manager);
        return ResponseEntity.ok(hotelDetails);
    }

    @PutMapping("/hotels/{hotelId}")
    public ResponseEntity<ManagerHotelDTO> updateHotel(
            @PathVariable Long hotelId,
            @RequestBody HotelUpdateRequestDto updateRequestDto, // ✅ DTO 변경
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        UserEntity manager = principalDetails.getUser();
        // 서비스 메서드에 새로운 DTO 전달
        ManagerHotelDTO updatedHotel = managerHotelService.updateHotel(hotelId, updateRequestDto, manager);
        return ResponseEntity.ok(updatedHotel);
    }

    @PostMapping(value = "/hotels/{hotelId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addImagesToHotel(
            @PathVariable Long hotelId,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        UserEntity manager = principalDetails.getUser();
        managerHotelService.addImagesToHotel(hotelId, images, manager);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/hotels/{hotelId}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long hotelId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        UserEntity manager = principalDetails.getUser();
        managerHotelService.deleteHotel(hotelId, manager);
        return ResponseEntity.ok().build();
    }
}