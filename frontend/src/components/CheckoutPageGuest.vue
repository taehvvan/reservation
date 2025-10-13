<template>
    <div class="checkout-page">
      <div class="content-wrapper">
        <div class="main-content">
          <nav class="breadcrumbs">
            <a href="#" @click.prevent="goBack">숙소 상세</a>
            <span>›</span>
            <span>비회원 예약/결제</span>
          </nav>
  
          <section class="checkout-section selected-room-section">
            <h2>선택하신 객실</h2>
            <div class="room-summary-card">
              <img :src="roomImage" :alt="roomName" class="selected-room-image">
              <div class="room-details-wrapper">
                <h3>{{ roomName }}</h3>
                <p class="hotel-name-small">{{ hotelName }}</p>
                <p class="location">📍 {{ location }}</p>
              </div>
            </div>
            <div class="date-picker">
              <div class="date-item">
                <label>체크인</label>
                <strong>{{ checkIn }}</strong>
              </div>
              <div class="nights">→ 1박 →</div>
              <div class="date-item">
                <label>체크아웃</label>
                <strong>{{ checkOut }}</strong>
              </div>
            </div>
          </section>
  
          <section class="checkout-section">
            <h2>예약자 정보 (필수)</h2>
            <div class="form-group">
              <label for="ordererName">예약자 이름</label>
              <input type="text" id="ordererName" v-model="ordererInfo.name" placeholder="이름을 입력하세요">
            </div>
            <div class="form-group">
              <label for="ordererPhone">휴대폰 번호</label>
              <input type="tel" id="ordererPhone" v-model="ordererInfo.phone" placeholder="'-' 없이 숫자만 입력하세요">
              <p class="form-guidance">입력하신 번호로 예약 정보가 발송됩니다.</p>
            </div>
          </section>
  
          <section class="checkout-section">
            <h2>결제</h2>
            <div class="payment-info-box">
              <p>안전하고 간편한 결제를 위해 <strong>토스페이먼츠</strong>를 사용합니다. 아래 결제하기 버튼을 누르면 토스 결제 창으로 이동합니다.</p>
            </div>
          </section>
  
        </div>
  
        <aside class="sidebar">
          <div class="order-summary">
            <div class="order-summary-header">
              <img :src="image" :alt="hotelName">
              <div class="hotel-info">
                <h3>{{ hotelName }}</h3>
                <p class="room-name-on-card">{{ roomName }}</p>
                <div class="rating-info">
                  <span class="score">{{ rating }}</span>
                  <span>아주 좋아요</span>
                  <span>·</span>
                  <span>후기 {{ reviews }}개</span>
                </div>
              </div>
            </div>
            <hr>
            <h2>요금 세부정보</h2>
            <div class="price-details">
              <div class="price-row">
                <span>기본 요금</span>
                <span>₩{{ (basePrice).toLocaleString() }}</span>
              </div>
              <div class="price-row">
                <span>세금 및 수수료</span>
                <span>₩{{ (taxes).toLocaleString() }}</span>
              </div>
              <hr class="total-divider">
              <div class="price-row total">
                <strong>총 결제 금액</strong>
                <strong>₩{{ (finalPrice).toLocaleString() }}</strong>
              </div>
            </div>
            <button class="btn-payment" @click="handlePayment">결제하기</button>
          </div>
        </aside>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  const props = defineProps({
    hotelName: { type: String, default: 'CVK Park Bosphorus 호텔' },
    roomName: { type: String, default: 'Superior room - 1 더블베드 or 2 트윈 베드' },
    basePrice: { type: Number, default: 240000 },
    image: { type: String, default: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80' },
    roomImage: { type: String, default: 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80' },
    rating: { type: Number, default: 4.2 },
    reviews: { type: Number, default: 56 },
    checkIn: { type: String, default: '9월 12일, 목요일' },
    checkOut: { type: String, default: '9월 13일, 금요일' },
    location: { type: String, default: 'Gümusuyu Mah. İnönü Cad. No:8, İstanbul 34437' },
    taxes: { type: Number, default: 24000 }
  });
  
  const ordererInfo = ref({ name: '', phone: '' });
  
  const finalPrice = computed(() => {
    return props.basePrice + props.taxes;
  });
  
  const tossPayments = ref(null);
  const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
  
  onMounted(() => {
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
  
  const goBack = () => {
    router.go(-1);
  };
  
  const handlePayment = async () => {
    if (!ordererInfo.value.name || !ordererInfo.value.phone) {
      alert("예약자 정보를 모두 입력해주세요.");
      return;
    }
    if (!tossPayments.value) {
      alert("결제 모듈이 준비되지 않았습니다.");
      return;
    }
    
    try {
      await tossPayments.value.requestPayment('card', {
        amount: finalPrice.value,
        orderId: orderId,
        orderName: `${props.hotelName} - ${props.roomName}`,
        customerName: ordererInfo.value.name,
        customerMobilePhone: ordererInfo.value.phone.replaceAll('-', ''),
        successUrl: `${window.location.origin}/payment/success`,
        failUrl: `${window.location.origin}/payment/fail`,
      });
    } catch (error) {
      console.error("결제 요청 실패:", error);
      if (error.code !== 'USER_CANCEL') {
        alert(`결제에 실패했습니다: ${error.message}`);
      }
    }
  };
  </script>
  
  <style scoped>
  @import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700;800&display=swap');
  
  .checkout-page {
    font-family: 'Noto Sans KR', sans-serif;
    background-color: #F9F9F9;
    padding: 40px 0;
    min-height: 100vh;
    color: #333;
  }
  .content-wrapper {
    max-width: 1000px;
    margin: 0 auto;
    padding: 0 20px;
    display: grid;
    grid-template-columns: 1.2fr 1fr;
    gap: 40px;
    align-items: flex-start;
  }
  .main-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .breadcrumbs {
    font-size: 0.9rem;
    color: #666;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .breadcrumbs a { color: #666; text-decoration: none; }
  .breadcrumbs a:hover { color: #0A2A66; }
  .breadcrumbs span:last-child { font-weight: 500; color: #333; }
  
  .checkout-section {
    background-color: #fff;
    border-radius: 12px;
    padding: 25px;
    border: 1px solid #EAEAEA;
  }
  .checkout-section h2 {
    font-size: 1.4rem;
    font-weight: 700;
    margin: 0 0 20px 0;
    color: #222;
  }
  
  .room-summary-card {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  .selected-room-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }
  .room-details-wrapper { flex-grow: 1; }
  .room-details-wrapper h3 { font-size: 1.3rem; margin: 0 0 5px 0; font-weight: 600; }
  .hotel-name-small, .location { font-size: 0.9rem; color: #666; margin: 2px 0; }
  .date-picker {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
  }
  .date-item { text-align: left; flex: 1; }
  .date-item label { font-size: 0.85rem; color: #888; display: block; }
  .date-item strong { font-size: 1.1rem; font-weight: 600; color: #444; }
  .nights { color: #999; text-align: center; flex-basis: 50px; }
  
  .form-group { margin-bottom: 15px; }
  .form-group label { display: block; font-weight: 500; margin-bottom: 8px; }
  .form-group input {
    width: 100%;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
    box-sizing: border-box;
  }
  .form-guidance { font-size: 0.85rem; color: #888; margin-top: 8px; }
  
  .payment-info-box { background-color: #f0f8ff; border-left: 4px solid #0A2A66; padding: 20px; font-size: 0.95rem; line-height: 1.6; }
  
  .sidebar { position: relative; }
  .order-summary {
    position: sticky;
    top: 40px;
    padding: 25px;
    background-color: #fff;
    border-radius: 12px;
    border: 1px solid #EAEAEA;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  .order-summary-header { display: flex; gap: 15px; align-items: flex-start; margin-bottom: 20px; }
  .order-summary-header img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; }
  .hotel-info h3 { font-size: 1.2rem; margin: 0 0 5px 0; font-weight: 600; }
  .hotel-info p { font-size: 0.95rem; color: #555; margin: 0; }
  .rating-info { display: flex; align-items: center; gap: 5px; font-size: 0.9rem; color: #777; margin-top: 8px; }
  .rating-info .score { background: #0A2A66; color: #fff; padding: 2px 6px; border-radius: 4px; font-weight: 700; }
  .order-summary hr { border: 0; border-top: 1px solid #eee; margin: 20px 0; }
  .order-summary h2 { font-size: 1.4rem; font-weight: 700; margin-bottom: 20px; }
  .price-details .price-row { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 1rem; }
  .total-divider { margin-top: 15px; }
  .price-details .price-row.total { font-size: 1.3rem; margin-top: 15px; font-weight: 800; }
  .btn-payment {
    width: 100%;
    padding: 14px;
    font-size: 1.2rem;
    font-weight: 700;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    background-color: #0A2A66;
    color: #fff;
    margin-top: 20px;
  }
  </style>