package com.example.backend.sms;

import com.example.backend.reservation.Reservation;
import com.example.backend.search.Hotel;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class SmsService {

    private final DefaultMessageService messageService;

    public SmsService(
            @Value("${coolsms.api.key}") String apiKey,
            @Value("${coolsms.api.secret}") String apiSecret
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize(
                apiKey,
                apiSecret,
                "https://api.coolsms.co.kr"
        );
    }

    @Value("${coolsms.sender.phone}")
    private String senderPhone;

    /**
     * 예약 정보를 바탕으로 고객에게 SMS를 발송하는 메서드
     */
    public void sendReservationDetailsSms(String recipientPhone, Reservation reservation) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            System.out.println("수신자 번호가 없어 SMS를 발송하지 않습니다. Order ID: " + reservation.getOrderId());
            return;
        }

        Hotel hotel = reservation.getHotel();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String checkIn = reservation.getCheckin().format(formatter);
        String checkOut = reservation.getCheckout().format(formatter);

        // 전송할 메시지 내용 구성
        String messageText = String.format(
                "[쉼, 한국] 예약완료!\n- 예약번호: %s\n- 호텔: %s\n- 체크인: %s\n- 체크아웃: %s\n- 결제금액: %,d원",
                reservation.getOrderId(),
                hotel.getHName(),
                checkIn,
                checkOut,
                reservation.getPrice()
        );

        Message message = new Message();
        message.setFrom(senderPhone);
        message.setTo(recipientPhone);
        message.setText(messageText);

        try {
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            System.out.println("SMS 발송 결과: " + response);
        } catch (Exception e) {
            System.err.println("SMS 발송 실패: " + e.getMessage());
        }
    }

    public void sendSms(String recipientPhone, String text) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            System.out.println("수신자 번호가 없어 SMS를 발송하지 않습니다.");
            return;
        }

        Message message = new Message();
        // 클래스 필드인 senderPhone을 사용하여 발신자 번호 설정
        message.setFrom(senderPhone);
        message.setTo(recipientPhone);
        message.setText(text);

        try {
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            System.out.println("SMS 발송 결과: " + response);
        } catch (Exception e) {
            System.err.println("SMS 발송 실패: " + e.getMessage());
        }
    }
}
