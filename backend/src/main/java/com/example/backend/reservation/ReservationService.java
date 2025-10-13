package com.example.backend.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import com.example.backend.search.Hotel;
import com.example.backend.search.HotelImage;
import com.example.backend.search.HotelRepository;
import com.example.backend.search.Room;
import com.example.backend.search.RoomRepository;
import com.example.backend.search.RoomAvailabilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;
    private final RoomAvailabilityService roomAvailabilityService;
    private final PaymentService paymentService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${image.local-path}")
    private String imageLocalPath;

    @Transactional
    public ReservationPrepareResponse createReservation(ReservationRequest request) {

        if (request.getCheckin() == null || request.getCheckout() == null) {
            throw new IllegalArgumentException("체크인 또는 체크아웃 날짜가 누락되었습니다.");
        }

        // 1. 객실 조회
        Room room = roomRepository.findById(request.getRId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 객실입니다. ID: " + request.getRId()));

                Hotel hotel = hotelRepository.findById(request.getHId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 호텔입니다. ID: " + request.getHId()));

        // 2. 유저 조회 (비회원인 경우 null)
        UserEntity user = null;
        if (request.getUId() != null) {
            user = userRepository.findById(request.getUId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + request.getUId()));
        }

        // 3. 예약 생성
        Reservation reservation = Reservation.builder()
                .room(room)
                .user(user)
                .hotel(hotel)
                .checkin(request.getCheckin())
                .checkout(request.getCheckout())
                .people(request.getPeople())
                .price(request.getPrice())
                .roomCount(request.getRoomCount())
                .status("예약 중")
                .createdAt(LocalDateTime.now())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 4. 프론트엔드에 필요한 모든 정보를 담은 DTO를 반환
        return new ReservationPrepareResponse(savedReservation);
    }

    public List<ReservationResponseDTO> findMyReservations(Integer userId) {
        // 1. 유저 ID로 Reservation 엔티티 목록을 조회합니다.
        List<Reservation> reservations = reservationRepository.findByUser_IdOrderByReIdDesc(userId);
        // 2. 스트림을 사용해 각 엔티티를 DTO로 변환합니다.
        return reservations.stream()
            // ✅ convertToDto 메서드에 필요한 두 인자를 전달하도록 변경
            .map(r -> this.convertToDto(r)) 
            .collect(Collectors.toList());
}

    // 👇 [추가] orderId로 예약 정보를 조회하는 메서드
    @Transactional(readOnly = true)
    public ReservationResponseDTO getReservationByOrderId(String orderId) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다. Order ID: " + orderId));
        return convertToDto(reservation);
    }

    // 👇 [수정] findGuestReservation 메서드 로직 변경
    public ReservationResponseDTO findGuestReservationByOrderId(String orderId, String phone) {
        Payment payment = paymentRepository.findByReservation_OrderIdAndPhone(orderId, phone)
            .orElse(null);
        
        if (payment == null || payment.getReservation() == null) {
            return null;
        }
        
        return convertToDto(payment.getReservation());
    }

    public Reservation findReservationById(Integer reservationId) {
        return reservationRepository.findById(reservationId).orElse(null);
    }

    @Transactional
    public Long cancelReservation(Integer reservationId) { // 반환 타입은 Long으로 유지
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
        
        if ("예약 취소".equals(reservation.getStatus())) {
            System.out.println("이미 취소된 예약입니다. Reservation ID: " + reservationId);
            return Long.valueOf(reservation.getPrice());
        }
        
        // 1. 결제 취소 시도 및 CancellationResponse 객체 받기
        PaymentService.CancellationResponse response = paymentService.cancelPayment(reservationId);

        // 2. 결과에 따라 분기 처리
        if (response.getResult() == PaymentService.CancellationResult.FAILED) {
            throw new RuntimeException("결제 시스템에서 취소 처리를 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
        
        // 3. 예약 상태 변경 및 재고 복원
        reservation.setStatus("예약 취소");
        reservationRepository.save(reservation);

        roomAvailabilityService.cancelRoomReservation(
            reservation.getRoom().getRId(),
            reservation.getCheckin(),
            reservation.getCheckout(),
            1
        );
        
        System.out.println("예약이 성공적으로 취소 처리되었습니다. Reservation ID: " + reservationId);
        
        // 4. 최종적으로 취소된 금액을 반환
        return response.getAmount();
    }

    

    @Transactional
    public void markAsDeleted(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약이 존재하지 않습니다."));

        String status = reservation.getStatus();

        if (!"예약 취소".equals(status) && !"리뷰 작성 완료".equals(status)) {
            throw new RuntimeException("취소된 예약만 삭제할 수 있습니다.");
        }
        
        reservation.setStatus("삭제됨");
        reservationRepository.save(reservation);
    }

    private ReservationResponseDTO convertToDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        
        Hotel hotel = reservation.getHotel();
        Room room = reservation.getRoom();

        // ★★★★★ 1. 호텔 이미지 URL을 찾고, 완전한 경로로 보정합니다. ★★★★★
        String hotelImageUrl = Optional.ofNullable(hotel)
                .flatMap(h -> h.getImages().stream()
                        // 'main' 타입 이미지를 우선적으로 찾습니다.
                        .filter(img -> "main".equalsIgnoreCase(img.getImageType()))
                        .map(HotelImage::getImageUrl)
                        .findFirst()
                        // 'main' 이미지가 없으면 첫 번째 이미지를 사용합니다.
                        .or(() -> h.getImages().stream().map(HotelImage::getImageUrl).findFirst()))
                .map(originalUrl -> {
                String webUrl;
                // 1. 로컬 경로를 환경 변수 기반의 웹 경로로 치환합니다.
                //    예: "D:/hotel_images/1.jpg" -> "https://api.domain.com/images/1.jpg"
                if (originalUrl != null && originalUrl.startsWith(imageLocalPath)) {
                    // Windows 경로 구분자(\\)를 웹 경로(/)로 먼저 통일합니다.
                    webUrl = originalUrl.replaceAll("\\\\", "/")
                                        .replace(imageLocalPath, baseUrl + "/images");
                } 
                // 2. 이미 웹 상대 경로(/images/...)인 경우 baseUrl을 앞에 붙여줍니다.
                else if (originalUrl != null && !originalUrl.startsWith("http")) {
                    webUrl = baseUrl + originalUrl;
                } 
                // 3. 이미 완전한 URL인 경우 그대로 반환합니다.
                else {
                    webUrl = originalUrl;
                }
                
                return webUrl;
            })
            .orElse(null); // 이미지가 하나도 없으면 null

        // ★★★★★ 2. 빌더를 사용하여 DTO를 생성합니다. ★★★★★
        return ReservationResponseDTO.builder()
                .reservationId(reservation.getReId())
                .orderId(reservation.getOrderId())
                .hotelName(hotel != null ? hotel.getHName() : "N/A")
                .roomType(room != null ? room.getType() : "N/A")
                .address(hotel != null ? hotel.getAddress() : "N/A")
                .hotelImage(hotelImageUrl) // 보정된 이미지 URL 사용
                .checkIn(reservation.getCheckin())
                .checkOut(reservation.getCheckout())
                .status(reservation.getStatus())
                .price(reservation.getPrice())
                .people(Optional.ofNullable(reservation.getPeople()).orElse(0))
                .roomCount(Optional.ofNullable(reservation.getRoomCount()).orElse(0))
                .hotelImage(hotelImageUrl)
                .build();
    }
}
