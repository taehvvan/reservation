<template>
  <div class="checkout-page">
    <div class="content-wrapper">
      <div class="main-content">
        <h1 class="page-title">예약/결제</h1>

        <section class="info-card">
          <h2 class="card-title">예약 정보</h2>
          <div class="hotel-summary">
            <img :src="roomImage" :alt="bookingStore.room?.type" class="hotel-thumbnail">
            <div class="hotel-info-text">
              <h3>{{ bookingStore.hotel?.hName }}</h3>
              <p>{{ bookingStore.room?.type }}</p>
            </div>
          </div>
          <div class="booking-details-grid">
            <div>
              <label>체크인</label>
              <p>{{ checkInText }}</p>
            </div>
            <div>
              <label>체크아웃</label>
              <p>{{ checkOutText }}</p>
            </div>
            <div>
              <label>객실 수, 예약 인원</label>
              <p>{{ bookingStore.rooms || 1 }} 개 / {{ bookingStore.guests || 1 }}명</p>
            </div>
          </div>
        </section>

        <section class="info-card">
          <h2 class="card-title">예약자 정보</h2>
          <div class="form-group">
            <label for="phone-number">휴대폰 번호</label>
            <input
              type="tel"
              id="phone-number"
              placeholder="'-' 없이 숫자 11자리를 입력하세요"
              v-model="phoneNumber"
              @input="formatPhoneNumber"
              maxlength="11"
            />
            <p class="form-guidance">입력하신 번호로 예약 정보가 발송됩니다.</p>
          </div>
        </section>

        <section class="info-card">
          <h2 class="card-title">할인</h2>
          <div v-if="authStore.isLoggedIn" class="coupon-box-member">
            <div class="coupon-input-group">
              <input type="text" :value="selectedCoupon ? `${formatDiscount(selectedCoupon)} 할인 쿠폰` : '쿠폰을 선택해주세요'" readonly placeholder="쿠폰을 선택해주세요">
              <button class="btn-coupon-modal" @click="isCouponModalVisible = true">쿠폰함</button>
            </div>
          </div>
          <div v-else class="coupon-box-guest">
            <div class="guest-cta-text">
              <h3>로그인하고 최대 혜택 받으세요</h3>
              <button class="btn-login-cta" @click="goToLogin">로그인 후 혜택 받기</button>
            </div>
            <div class="guest-cta-tags">
              <span class="cta-tag">🚀 회원 전용 쿠폰</span>
              <span class="cta-tag">✍️ 리뷰 작성 포인트</span>
              <span class="cta-tag">💎 멤버십 할인</span>
              <span class="cta-tag">🎁 회원 전용 특가</span>
            </div>
          </div>
        </section>

        <section class="info-card">
          <h2 class="card-title">결제 수단</h2>
          <div class="payment-info-box">
            <p>안전하고 간편한 결제를 위해 <strong>토스페이먼츠</strong>를 사용합니다.</p>
          </div>
        </section>
      </div>

      <aside class="sidebar">
        <div class="order-summary">
          <h2 class="summary-title">결제금액</h2>
          <div class="price-details">
            <div class="price-row">
              <span>객실 요금</span>
              <span>{{ basePrice.toLocaleString() }}원</span>
            </div>
            <div v-if="couponDiscount > 0" class="price-row highlight">
              <span>쿠폰 할인</span>
              <span>-{{ couponDiscount.toLocaleString() }}원</span>
            </div>
            <div class="price-row">
              <span>세금 및 수수료</span>
              <span>{{ taxes.toLocaleString() }}원</span>
            </div>
          </div>
          <div class="total-price">
            <strong>총 결제금액</strong>
            <strong>{{ finalPrice.toLocaleString() }}원</strong>
          </div>
          
          <div class="terms-agreement">
            <div class="agree-all">
              <label>
                <input type="checkbox" v-model="termsAgreed" />
                <span>아래 약관에 모두 동의합니다.</span>
              </label>
            </div>
            <ul class="terms-list">
              <li>개인정보 수집 및 이용 동의 (필수) <a href="/privacy" target="_blank">보기</a></li>
              <li>개인정보 제3자 제공 동의 (필수) <a href="/privacy" target="_blank">보기</a></li>
            </ul>
          </div>
          
          <button class="btn-payment" @click="handlePayment" :disabled="isLoading || !termsAgreed">
            {{ isLoading ? '처리 중...' : `${finalPrice.toLocaleString()}원 결제하기` }}
          </button>
        </div>
      </aside>
    </div>
  </div>
  
  <CouponModal 
    v-if="isCouponModalVisible"
    :coupons="availableCoupons"
    @close="isCouponModalVisible = false"
    @select-coupon="applyCoupon"
  />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import CouponModal from './CouponModal.vue';
import { useBookingStore } from '@/stores/booking';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const router = useRouter();
const bookingStore = useBookingStore();
const authStore = useAuthStore();

const isLoading = ref(true);
const phoneNumber = ref('');
const isCouponModalVisible = ref(false);
const selectedCoupon = ref(null);
const availableCoupons = ref([]);
const taxes = ref(5000);
const termsAgreed = ref(false);

const hotel = computed(() => bookingStore.hotel);
const room = computed(() => bookingStore.room);

const formatToLocalDate = (date) => {
  if (!date) return null;
  const d = new Date(date);
  return d.toISOString().split("T")[0]; // "2025-10-10"
};

const roomImage = computed(() => {
  if (!hotel.value?.type || !hotel.value?.hId) return 'https://via.placeholder.com/100x100?text=숙소';
  return `${import.meta.env.VITE_APP_API_URL}/images/${hotel.value.type}/${hotel.value.hId}.jpg`;
});

const nights = computed(() => {
  if (!bookingStore.checkIn || !bookingStore.checkout) return 1;
  const inDate = new Date(bookingStore.checkIn);
  const outDate = new Date(bookingStore.checkout);
  const diff = (outDate - inDate) / (1000 * 60 * 60 * 24);
  return diff > 0 ? diff : 1;
});

const basePrice = computed(() => {
  // [추가된 코드] room 정보나 체크인 정보가 없으면 계산을 시작하지 않고 0을 반환합니다.
  if (!bookingStore.room || !bookingStore.checkIn) {
    return 0;
  }
  
  const pricePerNight = bookingStore.room?.price || 0;
  return pricePerNight * nights.value * (bookingStore.rooms || 1);
});

const couponDiscount = computed(() => {
  if (!selectedCoupon.value || !authStore.isLoggedIn) return 0;
  if (selectedCoupon.value.type === 'PERCENT') {
    return Math.floor(basePrice.value * (selectedCoupon.value.discount / 100));
  }
  return selectedCoupon.value.discount || 0;
});

const finalPrice = computed(() => basePrice.value - couponDiscount.value + taxes.value);

const formatDate = (date) => {
    if (!date) return '';
    const d = new Date(date);
    const options = { month: 'long', day: 'numeric', weekday: 'long' };
    return d.toLocaleDateString('ko-KR', options);
}
const checkInText = computed(() => formatDate(bookingStore.checkIn));
const checkOutText = computed(() => formatDate(bookingStore.checkout));

// [추가된 함수] 숫자만 입력되도록 실시간으로 필터링합니다.
const formatPhoneNumber = (event) => {
  const cleaned = event.target.value.replace(/\D/g, '');
  phoneNumber.value = cleaned;
};

const tossPayments = ref(null);
const clientKey = 'test_ck_QbgMGZzorzeozAo5yPg7Vl5E1em4';

const fetchAvailableCoupons = async () => {
    if (!authStore.isLoggedIn) return;
    try {
        const token = localStorage.getItem('accessToken');
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/coupons/my-coupons`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        availableCoupons.value = response.data;
    } catch (error) {
        console.error("사용 가능한 쿠폰 목록을 불러오는 데 실패했습니다:", error);
    }
};

onMounted(async () => {
  try {
    if (!bookingStore.room) {
      await bookingStore.fetchBookingDetails();
    }
    await fetchAvailableCoupons();
  } catch (error) {
    console.error("예약 정보를 불러오는 데 실패했습니다:", error);
    alert("예약 정보를 불러오지 못했습니다. 이전 페이지로 돌아갑니다.");
    router.go(-1);
  } finally {
    isLoading.value = false;
  }

  const script = document.createElement('script');
  script.src = "https://js.tosspayments.com/v1";
  script.onload = () => {
    try {
      tossPayments.value = TossPayments(clientKey);
    } catch (e) {
      console.error("Toss Payments SDK 초기화 실패:", e);
    }
  };
  document.head.appendChild(script);
});

const goToLogin = () => router.push('/login');

const applyCoupon = (coupon) => {
  selectedCoupon.value = coupon;
  isCouponModalVisible.value = false;
};

const formatDiscount = (coupon) => {
  if (coupon.type === 'PERCENT') return `${coupon.discount}%`;
  if (coupon.type === 'FIXED') return `${coupon.discount.toLocaleString()}원`;
  return '';
};


const handlePayment = async () => {
  // --- 휴대폰 번호 유효성 검사 ---
  if (!authStore.isLoggedIn && !phoneNumber.value.trim()) {
    alert("비회원 예약을 위해 휴대폰 번호를 입력해주세요.");
    return;
  }
  if (phoneNumber.value) {
    const phoneRegex = /^010\d{8}$/;
    if (!phoneRegex.test(phoneNumber.value)) {
      alert("올바른 휴대폰 번호 11자리를 입력해주세요. (예: 01012345678)");
      return;
    }
  }
  // --- 유효성 검사 끝 ---

  if (authStore.isLoading) {
    alert("사용자 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
    return;
  }
  if (!bookingStore.room || !bookingStore.room.rId) {
    return alert('예약 정보가 올바르지 않습니다. 페이지를 새로고침 후 다시 시도해주세요.');
  }
  if (!termsAgreed.value) {
    alert("결제 약관에 동의해주세요.");
    return;
  }

  const room = bookingStore.room;
  const hotel = bookingStore.hotel;

  let availableCount = room.availabilities.length > 0
    ? Math.min(...room.availabilities.map(a => a.availableCount))
    : room.count;

  if (availableCount <= 0) {
    return alert('죄송합니다. 선택한 날짜에 잔여 객실이 없습니다.');
  }
  if (!tossPayments.value) {
    return alert('결제 모듈이 준비되지 않았습니다.');
  }
  if (!room || !hotel) {
    return alert('예약 정보가 올바르지 않습니다.');
  }

  try {
    const prepareResponse = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/reservations/prepare`, {
      rId: bookingStore.room.rId,
      hId: bookingStore.hotel.hId,
      uId: authStore.isLoggedIn ? authStore.userId : null,
      checkin: formatToLocalDate(bookingStore.checkIn),   // ✅ 변환된 날짜
      checkout: formatToLocalDate(bookingStore.checkout), // ✅ 변환된 날짜
      people: bookingStore.guests,
      price: finalPrice.value,
      roomCount: bookingStore.rooms,
    });

    const { orderId, reservationId } = prepareResponse.data;

    if (!orderId || !reservationId) {
      throw new Error("서버로부터 유효한 예약 정보를 받지 못했습니다.");
    }

    const paymentInfo = {
      reservationId: reservationId,
      roomId: bookingStore.room.rId,
      hotelId: bookingStore.hotel.hId,
      userId: authStore.isLoggedIn ? authStore.userId : null,
      phone: phoneNumber.value,
      userCouponId: selectedCoupon.value ? selectedCoupon.value.userCouponId : null,
    };
    localStorage.setItem('paymentInfo', JSON.stringify(paymentInfo));

    await tossPayments.value.requestPayment('card', {
      amount: finalPrice.value,
      orderId: orderId,
      orderName: `${bookingStore.hotel.hName} - ${bookingStore.room.type}`,
      customerName: authStore.userName || '비회원 고객',
      successUrl: `${window.location.origin}/payment-callback`,
      failUrl: `${window.location.origin}/payment-fail`,
    });
  } catch (error) {
    console.error('결제 처리 중 오류 발생:', error);
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류가 발생했습니다.';
    alert(`결제 처리 중 오류가 발생했습니다: ${errorMessage}`);
    localStorage.removeItem('paymentInfo');
  }
};
</script>

<style scoped>
/* 전체 페이지 레이아웃 */
.checkout-page { font-family: 'Noto Sans KR', sans-serif; background-color: #f8f9fa; padding: 50px 0; min-height: 100vh; }
.content-wrapper { max-width: 1100px; margin: 0 auto; padding: 0 20px; display: grid; grid-template-columns: 1.5fr 1fr; gap: 40px; align-items: flex-start; }
.main-content, .sidebar { min-width: 0; }
.page-title { font-size: 2rem; font-weight: 800; margin-bottom: 30px; }

/* 정보 카드 공통 스타일 */
.info-card { background-color: #fff; border-radius: 12px; padding: 25px; margin-bottom: 25px; border: 1px solid #e9ecef; }
.card-title { font-size: 1.3rem; font-weight: 700; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f1f3f5; }

/* 예약 정보 카드 */
.hotel-summary { display: flex; align-items: center; gap: 15px; }
.hotel-thumbnail { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; }
.hotel-info-text h3 { font-size: 1.2rem; font-weight: 600; margin: 0 0 5px; }
.hotel-info-text p { font-size: 1rem; color: #555; margin: 0; }
.booking-details-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-top: 20px; padding-top: 20px; border-top: 1px solid #f1f3f5; }
.booking-details-grid label { font-size: 0.9rem; color: #888; margin-bottom: 5px; display: block; }
.booking-details-grid p { font-size: 1rem; font-weight: 600; margin: 0; }

/* 예약자 정보 카드 */
.form-group { display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-weight: 600; font-size: 1rem; }
.form-group input { width: 100%; box-sizing: border-box; padding: 12px 15px; border: 1px solid #dee2e6; border-radius: 8px; font-size: 1rem; }
.form-group input:focus { outline: none; border-color: #0A2A66; box-shadow: 0 0 0 3px rgba(10, 42, 102, 0.1); }
.form-guidance { font-size: 0.85rem; color: #868e96; margin-top: 5px; }

/* 회원 쿠폰 박스 */
.coupon-input-group { display: flex; gap: 10px; }
.coupon-input-group input { flex-grow: 1; background-color: #f8f9fa; cursor: pointer; }
.btn-coupon-modal { background-color: #495057; color: #fff; border: none; padding: 0 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }

/* 비회원 쿠폰 박스 (요청 디자인) */
.coupon-box-guest { display: flex; align-items: center; justify-content: space-between; background-color: #e7f5ff; border-radius: 12px; padding: 25px; }
.guest-cta-text h3 { margin: 0 0 15px; font-size: 1.3rem; font-weight: 700; color: #1864ab; }
.btn-login-cta { background-color: #fff; color: #1c7ed6; border: 1px solid #1c7ed6; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-login-cta:hover { background-color: #1c7ed6; color: #fff; }
.guest-cta-tags { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.cta-tag { background-color: #fff; padding: 8px 12px; border-radius: 20px; font-size: 0.85rem; font-weight: 500; color: #495057; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }

/* 결제 수단 */
.payment-info-box { font-size: 1rem; color: #495057; }

/* 사이드바 결제 요약 */
.sidebar { position: relative; }
.order-summary { position: sticky; top: 40px; background-color: #fff; border: 1px solid #e9ecef; border-radius: 12px; padding: 25px; }
.summary-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 20px; }
.price-details { display: flex; flex-direction: column; gap: 15px; padding-bottom: 20px; border-bottom: 1px solid #f1f3f5; }
.price-row { display: flex; justify-content: space-between; align-items: center; font-size: 1rem; }
.price-row span:first-child { color: #555; }
.price-row span:last-child { font-weight: 600; }
.price-row.highlight { color: #e64980; }
.total-price { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 20px; }
.total-price strong:first-child { font-size: 1.1rem; font-weight: 600; }
.total-price strong:last-child { font-size: 1.8rem; font-weight: 800; color: #0A2A66; }

/* 약관 동의 */
.terms-agreement { margin-top: 30px; background-color: #f8f9fa; padding: 15px; border-radius: 8px; }
.agree-all { padding-bottom: 10px; border-bottom: 1px solid #e9ecef; margin-bottom: 10px; }
.agree-all label { display: flex; align-items: center; font-size: 1.1rem; font-weight: 600; cursor: pointer; }
.agree-all input { width: 20px; height: 20px; margin-right: 10px; }
.terms-list { list-style: none; padding: 0; margin: 0; font-size: 0.9rem; color: #868e96; }
.terms-list li { display: flex; justify-content: space-between; margin-bottom: 5px; }
.terms-list a { color: #868e96; text-decoration: underline; }

.save-phone-row {
display: flex;
align-items: center;
gap: 8px;
font-size: 0.9rem;
color: #555;
}

.checkbox-label {
display: flex;
align-items: center;
gap: 4px;
margin: 0;
font-weight: normal;
}

.checkbox-label input[type="checkbox"] {
width: 16px;
height: 16px;
}

/* 결제 버튼 */
.btn-payment { width: 100%; padding: 16px; font-size: 1.2rem; font-weight: 700; border-radius: 10px; cursor: pointer; background-color: #0A2A66; color: #fff; border: none; margin-top: 25px; transition: all 0.2s; }
.btn-payment:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(10, 42, 102, 0.2); }
.btn-payment:disabled { background-color: #ced4da; cursor: not-allowed; }
</style>