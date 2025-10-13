<template>
  <div class="payment-success-page">
    <div class="content-wrapper">
      <h1>🎉 결제가 성공적으로 완료되었습니다!</h1>
      <p>예약이 정상적으로 처리되었습니다. 이용해주셔서 감사합니다.</p>

      <div class="receipt-box" v-if="reservationDetails">
          <h2 class="receipt-title">예약 상세 정보</h2>
          <div class="receipt-item">
            <span>예약 번호</span>
            <strong>{{ reservationDetails.orderId }}</strong>
          </div>
          <div class="receipt-item">
            <span>호텔</span>
            <span>{{ reservationDetails.hotelName }}</span>
          </div>
          <div class="receipt-item">
            <span>체크인</span>
            <span>{{ formatDate(reservationDetails.checkIn) }}</span>
          </div>
          <div class="receipt-item">
            <span>체크아웃</span>
            <span>{{ formatDate(reservationDetails.checkOut) }}</span>
          </div>
          <div class="receipt-item total">
            <span>최종 결제 금액</span>
            <strong>{{ reservationDetails.price.toLocaleString() }}원</strong>
          </div>
        </div>
        <div v-else class="loading-box">
          <p>예약 정보를 불러오는 중입니다...</p>
        </div>

      <div class="info-box" v-if="orderId">
        <strong>예약 번호:</strong>

        <!-- 번호 + 아이콘 + 뱃지 -->
        <div class="order-line">
          <!-- 번호 자체 클릭 복사 -->
          <button
            class="order-id-btn"
            type="button"
            @click="copyOrderId"
            :aria-label="`예약 번호 ${orderId} 복사하기`"
            title="클릭하여 복사"
          >
            {{ orderId }}
          </button>

          <!-- 작게 붙는 카피 아이콘 버튼 -->
          <button
            class="icon-copy-btn"
            type="button"
            @click="copyOrderId"
            aria-label="예약 번호 복사"
            title="복사"
          >
            <!-- Inline SVG (copy) -->
            <svg class="copy-icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path d="M16 1H4a2 2 0 0 0-2 2v12h2V3h12V1zm3 4H8a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2zm0 16H8V7h11v14z"/>
            </svg>
          </button>

          <!-- 복사됨 뱃지 -->
          <span class="copy-badge" v-show="copied" aria-live="polite">복사됨</span>
        </div>
      </div>

      <div class="button-group">
        <button class="btn-home" @click="goHome">홈으로 돌아가기</button>
        <button class="btn-bookings" @click="goToBookings">예약 내역 확인하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useBookingStore } from '../stores/booking';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const bookingStore = useBookingStore();

const reservationDetails = ref(null);
const orderId = ref('');
const copied = ref(false);
let copyTimer = null;

// [수정] 날짜 포맷팅을 위한 헬퍼 함수
const formatDate = (dateString) => {
  if (!dateString) return '';
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
};

onMounted(async () => {
    // 1. URL 쿼리에서 orderId를 가져옵니다.
    orderId.value = route.query.orderId || '';

    if (orderId.value) {
      try {
        const token = localStorage.getItem('accessToken');
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

        // 2. [핵심] orderId를 사용해 백엔드로부터 예약 상세 정보 조회 API 호출
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/reservations/order/${orderId.value}`, { headers });
        
        // 3. 응답 데이터를 reservationDetails에 저장하여 화면에 표시
        reservationDetails.value = response.data;

      } catch (error) {
        console.error("예약 정보를 불러오는 데 실패했습니다:", error);
        // 에러 발생 시 사용자에게 알림을 줄 수 있습니다.
        alert("예약 정보를 불러오는 데 실패했습니다. 예약 내역 페이지에서 확인해주세요.");
      }
    }
    
    // 4. 불필요해진 로컬 스토리지의 임시 예약 정보를 삭제합니다.
    bookingStore.clearBooking();
  });

async function copyOrderId() {
  if (!orderId.value) return;
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(orderId.value);
    } else {
      const ta = document.createElement('textarea');
      ta.value = orderId.value;
      ta.setAttribute('readonly', '');
      ta.style.position = 'fixed';
      ta.style.left = '-9999px';
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
    }
    copied.value = true;
    if (copyTimer) clearTimeout(copyTimer);
    copyTimer = setTimeout(() => (copied.value = false), 1500);
  } catch (e) {
    console.error('클립보드 복사 실패:', e);
  }
}

const goHome = () => router.push('/');
const goToBookings = () => {
  if (authStore.isLoggedIn) router.push('/mypage');
  else router.push('/booking-check');
};

console.log('결제 성공 orderId:', route.query.orderId);
</script>

<style scoped>
.payment-success-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  text-align: center;
  padding: 40px 20px;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: #f9f9f9;
  color: #333;
}

.content-wrapper {
  background-color: #fff;
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  max-width: 600px;
  width: 100%;
}

h1 { font-size: 1.8rem; margin-bottom: 20px; color: #0A2A66; }
p { font-size: 1rem; margin-bottom: 30px; color: #555; }

.info-box {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  background: #f1f6ff;
  border: 1px solid #d6e4ff;
  padding: 12px 14px;
  border-radius: 10px;
  margin: 0 auto 20px;
}

/* 번호 + 아이콘 줄 */
.order-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 클릭 가능한 예약번호 버튼 */
.order-id-btn {
  background: #eef4ff;
  border: 1px dashed #0A2A66;
  padding: 6px 10px;
  border-radius: 8px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.95rem;
  cursor: pointer;
  transition: transform 0.08s ease, background-color 0.15s ease, box-shadow 0.15s ease;
}
.order-id-btn:hover { background: #e3edff; box-shadow: 0 1px 6px rgba(10,42,102,0.12); }
.order-id-btn:active { transform: scale(0.98); }

/* 작은 카피 아이콘 버튼 */
.icon-copy-btn {
  width: 28px;
  height: 28px;
  min-width: 28px;
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border: 1px solid #cdd7ee;
  border-radius: 6px;
  cursor: pointer;
  padding: 0;
  transition: background-color 0.15s ease, box-shadow 0.15s ease, transform 0.08s ease;
}
.icon-copy-btn:hover { background: #f5f8ff; box-shadow: 0 1px 6px rgba(10,42,102,0.12); }
.icon-copy-btn:active { transform: scale(0.96); }

.copy-icon {
  display: block;
  width: 18px;
  height: 18px;
  fill: #0A2A66;
}

/* 복사됨 뱃지 */
.copy-badge {
  display: inline-block;
  background: #eaffee;
  color: #1a7f37;
  border: 1px solid #b7efc2;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.9rem;
  user-select: none;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}
.button-group button {
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease-in-out;
}
.btn-home { background-color: #0A2A66; color: #fff; }
.btn-home:hover { background-color: #09305a; }
.btn-bookings { background-color: #fff; color: #0A2A66; border: 2px solid #0A2A66; }
.btn-bookings:hover { background-color: #0A2A66; color: #fff; }

/* [추가] 영수증 및 로딩 박스 스타일 */
  .receipt-box {
    text-align: left;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    padding: 20px;
    margin-top: 25px;
    margin-bottom: 20px;
    background-color: #f8f9fa;
  }

  .receipt-title {
    font-size: 1.2rem;
    font-weight: 700;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #dee2e6;
  }

  .receipt-item {
    display: flex;
    justify-content: space-between;
    font-size: 1rem;
    margin-bottom: 12px;
  }

  .receipt-item span:first-child {
    color: #868e96;
  }
  
  .receipt-item span:last-child, .receipt-item strong {
    font-weight: 600;
    color: #495057;
  }

  .receipt-item.total {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px dashed #ced4da;
  }

  .receipt-item.total span:first-child {
    font-size: 1.1rem;
    color: #343a40;
  }
  
  .receipt-item.total strong {
    font-size: 1.3rem;
    color: #0A2A66;
  }

  .loading-box {
    padding: 40px 0;
    color: #888;
  }
</style>
