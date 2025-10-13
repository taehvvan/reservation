package com.example.backend.reservation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // Jackson이 유사시 사용할 기본 생성자
public class PaymentRequest {

    // Toss Payments 필수 정보
    private String paymentKey;
    private String orderId;
    private Long amount;

    // 우리 시스템에서 필요한 추가 정보
    private Integer userId;
    private String phone;
    private String payMethod; // 결제 수단
    private Long userCouponId;

    // 클라이언트에서 넘어올 수 있는 기타 정보들 (오류 방지용)
    private Integer pId;
    private Integer reId;
    private Integer rId;
    private Long hId; // hId의 타입이 Long일 수 있으므로 Long으로 변경
    private String checkin;
    private String checkout;
    private Integer quantity;

    /**
     * Jackson이 JSON 데이터를 이 객체로 변환할 때 사용할 생성자임을 명시합니다.
     * @JsonProperty는 JSON의 각 필드 이름을 생성자의 파라미터와 정확히 매핑해줍니다.
     */
    @JsonCreator
    public PaymentRequest(
            @JsonProperty("paymentKey") String paymentKey,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("amount") Long amount,
            @JsonProperty("userId") Integer userId,
            @JsonProperty("phone") String phone,
            @JsonProperty("payMethod") String payMethod,
            @JsonProperty("userCouponId") Long userCouponId,
            @JsonProperty("pId") Integer pId,
            @JsonProperty("reId") Integer reId,
            @JsonProperty("rId") Integer rId,
            @JsonProperty("hId") Long hId,
            @JsonProperty("checkin") String checkin,
            @JsonProperty("checkout") String checkout,
            @JsonProperty("quantity") Integer quantity) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.userId = userId;
        this.phone = phone;
        this.payMethod = payMethod;
        this.userCouponId = userCouponId;
        this.pId = pId;
        this.reId = reId;
        this.rId = rId;
        this.hId = hId;
        this.checkin = checkin;
        this.checkout = checkout;
        this.quantity = quantity;
    }
}