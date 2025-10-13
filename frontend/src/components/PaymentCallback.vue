<template>
  <div>결제 완료 처리 중... 잠시만 기다려주세요.</div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

onMounted(async () => {

  const rawRoomId = localStorage.getItem('roomId');
  console.log("--- 최종 디버깅 ---");
  console.log("localStorage에서 가져온 raw roomId:", rawRoomId);
  console.log("Number(rawRoomId) 변환 결과:", Number(rawRoomId));
  console.log("-------------------");

  // [디버깅] 현재 localStorage 상태 확인
  console.log("현재 localStorage:", JSON.stringify(localStorage, null, 2));

  /*
  // 1. 토스페이먼츠에서 받은 결제 정보 추출
  const query = route.query;
  const orderId = Array.isArray(query.orderId) ? query.orderId[0] : query.orderId;
  const paymentKey = Array.isArray(query.paymentKey) ? query.paymentKey[0] : query.paymentKey;
  const amount = Array.isArray(query.amount) ? query.amount[0] : query.amount;
  */

  // 1. 토스페이먼츠에서 받은 결제 정보 추출
  const { paymentKey, orderId, amount } = route.query;

  if (!orderId || !paymentKey || !amount) return router.replace('/');

  // 2. localStorage에서 저장했던 paymentInfo 객체 불러오기
  const storedPaymentInfo = localStorage.getItem('paymentInfo');

  if (!paymentKey || !orderId || !amount || !storedPaymentInfo) {
    console.error("결제 처리 실패: 필수 정보가 누락되었습니다.");
    // 실패 시 localStorage 정리
    localStorage.removeItem('paymentInfo');
    return router.replace('/');
  }

  const paymentInfo = JSON.parse(storedPaymentInfo);

  try {
    const token = localStorage.getItem('accessToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    const requestData = {
      reId: paymentInfo.reservationId,
      rId: paymentInfo.roomId,
      hId: paymentInfo.hotelId,
      orderId: orderId,
      userId: paymentInfo.userId,
      phone: paymentInfo.phone,
      paymentKey,
      amount: Number(amount),
      payMethod: 'TOSS_PAY',
      userCouponId: paymentInfo.userCouponId,
    };

    console.log('백엔드로 전송할 최종 데이터:', requestData);

    // 3. 결제 완료 API 호출
    await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/payments/complete`, requestData, { headers });

    // 4. localStorage 정리
    localStorage.removeItem('paymentInfo');

    router.push({
      path: '/payment-success',
      query: { orderId },
      replace: true
    });
  } catch (err) {
    console.error("결제 완료 처리 중 오류:", err);
    router.replace(`/payment-success?orderId=${orderId}`);
  }
});
</script>