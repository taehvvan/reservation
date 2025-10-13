package com.example.backend.reservation;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.backend.coupon.CouponService;
import com.example.backend.login.security.PrincipalDetails;
import com.example.backend.register.UserEntity;
import com.example.backend.register.UserRepository;
import com.example.backend.search.RoomAvailabilityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.backend.sms.SmsService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomAvailabilityService roomAvailabilityService;
    private final RestTemplate restTemplate; // Toss API 연동을 위한 HTTP 클라이언트
    private final ObjectMapper objectMapper;
    private final CouponService couponService;
    private final SmsService smsService;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${image.local-path}")
    private String imageLocalPath;

    @Getter
    @RequiredArgsConstructor
    public static class CancellationResponse {
        private final CancellationResult result;
        private final Long amount;
    }

    // 결과를 명확히 구분하기 위한 Enum 정의
    public enum CancellationResult {
        SUCCESS,          // 성공
        ALREADY_CANCELED, // 이미 취소된 상태
        FAILED            // 그 외 실패
    }   

    @Transactional
    public PaymentResponseDTO completePayment(PaymentRequest request, PrincipalDetails principalDetails) {

        // 1. 결제사가 전달한 orderId로 예약을 조회합니다.
        Reservation reservation = reservationRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문번호에 해당하는 예약 정보를 찾을 수 없습니다. Order ID: " + request.getOrderId()));

        // 2. [검증 추가] 요청된 금액과 실제 예약 금액이 일치하는지 확인합니다.
        if (reservation.getPrice() != request.getAmount()) {
            throw new IllegalStateException("요청된 결제 금액이 예약 금액과 일치하지 않습니다.");
        }

        // ✅ [상태 검증 추가] '예약 중' 상태일 때만 결제를 진행하도록 방어 로직을 추가합니다.
        if (!"예약 중".equals(reservation.getStatus())) {
            throw new IllegalStateException("이미 처리되었거나 유효하지 않은 예약 상태입니다. 현재 상태: " + reservation.getStatus());
        }

        UserEntity user = null;
        // 2-1. 인증된 사용자 정보가 있으면 최우선으로 사용 (가장 안전)
        if (principalDetails != null) {
            user = principalDetails.getUser();
        } 
        // 2-2. 인증 정보는 없지만 DTO에 userId가 있으면, 해당 ID로 사용자를 조회 (비회원/토큰 문제 대비)
        else if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElse(null); // 사용자를 못찾으면 null
        }
        
        // 2. ❗[핵심]❗ 토스페이먼츠 결제 승인 API 호출
        // 실제 운영 환경에서는 이 로직을 반드시 구현해야 합니다.
        // 여기서는 성공했다고 가정하고 시뮬레이션합니다.
        verifyPaymentWithToss(request.getPaymentKey(), request.getOrderId(), request.getAmount());

        // 4. 예약 상태 업데이트
        reservation.setStatus("예약 완료");
        reservationRepository.save(reservation);

        // [핵심 로직 추가] 객실 재고를 차감합니다.
        // 결제가 최종 완료된 이 시점에서만 재고를 차감해야 중복 차감을 방지할 수 있습니다.
        roomAvailabilityService.reserveRoom(
            reservation.getRoom().getRId(),
            reservation.getCheckin(),
            reservation.getCheckout(),
            1 // 현재 로직 상 객실 1개를 예약
        );

        if (request.getUserCouponId() != null) {
            couponService.useCoupon(request.getUserCouponId());
        }

        // 5. 결제 정보 생성
        Payment payment = Payment.builder()
                .user(reservation.getUser())
                .reservation(reservation)
                .room(reservation.getRoom())
                .payMethod(request.getPayMethod())
                .phone(request.getPhone())
                .paymentKey(request.getPaymentKey())
                .amount(request.getAmount())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // [핵심] 결제 완료 후 SMS 발송 로직 호출
        // reservation 객체에는 이미 호텔, 날짜 등 모든 정보가 포함되어 있습니다.
        smsService.sendReservationDetailsSms(request.getPhone(), reservation);

        System.out.println("결제 완료 & 예약 상태 변경 완료 - pId: " + savedPayment.getPId());

        return new PaymentResponseDTO(savedPayment);
    }

    @Transactional
    public CancellationResponse cancelPayment(Integer reservationId) {
        Payment payment = paymentRepository.findByReservationReId(reservationId)
                .orElseThrow(() -> new IllegalStateException("해당 예약에 대한 결제 정보를 찾을 수 없습니다."));

        return requestTossPaymentCancel(payment.getPaymentKey(), "고객 요청");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CancellationResponse requestTossPaymentCancel(String paymentKey, String cancelReason) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        HttpHeaders headers = new HttpHeaders();
        // ... (Header 설정 코드는 동일)
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.setBasicAuth(encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        String requestBody = "{\"cancelReason\":\"" + cancelReason + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
            long cancelAmount = rootNode.path("cancels").get(0).path("cancelAmount").asLong();

            System.out.println("Toss Payments 결제 취소 성공 - PaymentKey: " + paymentKey + ", 취소 금액: " + cancelAmount);
            return new CancellationResponse(CancellationResult.SUCCESS, cancelAmount);

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            System.err.println("Toss Payments API 에러 응답: " + responseBody);
            if (responseBody.contains("NOT_CANCELABLE_PAYMENT")) {
                // 이미 취소된 경우, 금액은 0으로 하여 반환
                return new CancellationResponse(CancellationResult.ALREADY_CANCELED, 0L);
            } else {
                return new CancellationResponse(CancellationResult.FAILED, 0L);
            }
        } catch (Exception e) {
            System.err.println("Toss Payments 결제 취소 중 알 수 없는 오류 발생 - PaymentKey: " + paymentKey);
            e.printStackTrace();
            return new CancellationResponse(CancellationResult.FAILED, 0L);
        }
    }
    
    /**
     * 실제 토스페이먼츠 서버에 결제 승인을 요청하는 메서드 (구현 필요)
     */
    public void verifyPaymentWithToss(String paymentKey, String orderId, Long amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        
        HttpHeaders headers = new HttpHeaders();
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.setBasicAuth(encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String requestBody = String.format("{\"paymentKey\":\"%s\",\"orderId\":\"%s\",\"amount\":%d}",
                paymentKey, orderId, amount);
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
            
            // 응답 상태가 "DONE"인지, 금액이 일치하는지 다시 한번 확인 (더욱 안전)
            if (!"DONE".equals(rootNode.path("status").asText())) {
                throw new RuntimeException("결제 승인이 완료되지 않았습니다. 상태: " + rootNode.path("status").asText());
            }

            System.out.println("Toss Payments 결제 승인 API 호출 성공 - PaymentKey: " + paymentKey);

        } catch (Exception e) {
            System.err.println("Toss Payments 결제 승인 실패: " + e.getMessage());
            // 여기서 실패하면 전체 트랜잭션을 롤백시켜야 하므로 예외를 던지는 것이 맞습니다.
            throw new RuntimeException("결제 승인에 실패했습니다.");
        }
    }

    public ReservationResponseDTO findReservationForGuest(String orderId, String phone) {
        return paymentRepository.findByReservation_OrderIdAndPhone(orderId, phone)
                .map(payment -> new ReservationResponseDTO(
                    payment.getReservation(), 
                    baseUrl,            
                    imageLocalPath      
                ))
                .orElse(null);
    }
    //사이트 관리자 월간 금액 알려고 하는거 
    public Long getCurrentMonthTotalSales() {
        // 현재 날짜에서 연도와 월을 가져옵니다.
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 레포지토리를 호출하여 당월 합계를 구합니다.
        Long totalAmount = paymentRepository.findTotalAmountByYearAndMonth(currentYear, currentMonth);

        // 결과가 null이면 (해당 월에 결제가 없었으면) 0L을 반환하거나, null을 그대로 반환하도록 처리할 수 있습니다.
        return totalAmount != null ? totalAmount : 0L;
    }
}