<template>
    <div class="booking-detail-container" v-if="reservation">
      <header class="page-header">
        <h1>예약 상세 정보</h1>
        <p>예약 번호 {{ reservation.reservationId }}의 상세 내역입니다.</p>
      </header>
  
      <section class="reservation-info">
        <h2>숙소 정보</h2>
        <div class="hotel-info">
          <img :src="reservation.hotelImage || 'https://via.placeholder.com/300x200'" alt="호텔 이미지" class="hotel-image">
          <div class="hotel-details">
            <p><strong>호텔명:</strong> {{ reservation.hotelName }}</p>
            <p><strong>주소:</strong> {{ reservation.address }}</p>
            <p><strong>객실 타입:</strong> {{ reservation.roomType }}</p>
          </div>
        </div>
      </section>
  
      <section class="reservation-dates">
        <h2>예약 정보</h2>
        <p><strong>체크인:</strong> {{ formatDate(reservation.checkIn) }}</p>
        <p><strong>체크아웃:</strong> {{ formatDate(reservation.checkOut) }}</p>
        <p><strong>인원:</strong> {{ reservation.people }}명</p>
        <p><strong>가격:</strong> {{ reservation.price.toLocaleString() }}원</p>
        <p><strong>상태:</strong> {{ reservation.status }}</p>
      </section>
  
    </div>
  
    <div v-else class="loading">
      예약 정보를 불러오는 중입니다...
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import axios from 'axios';
  
  const route = useRoute();
  const reservation = ref(null);
  
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  };
  
  onMounted(async () => {
    const reservationId = route.query.reservationId;
    if (!reservationId) return;
  
    try {
      const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/reservations/guest/detail`, {
        params: { reservationId }
      });
  
      reservation.value = response.data;
  
      if (!reservation.value) {
        alert('예약 정보를 찾을 수 없습니다.');
      }
    } catch (err) {
      console.error('예약 상세 조회 실패:', err);
      alert('예약 정보를 불러오는데 실패했습니다.');
    }
  });
  </script>
  
  <style scoped>
  .booking-detail-container {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
    background-color: #fff;
    border-radius: 16px;
    box-shadow: 0 8px 30px rgba(0,0,0,0.05);
  }
  
  .page-header {
    text-align: center;
    margin-bottom: 30px;
  }
  
  .page-header h1 {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 10px;
  }
  
  .reservation-info, .reservation-dates {
    margin-bottom: 30px;
  }
  
  .hotel-info {
    display: flex;
    gap: 20px;
  }
  
  .hotel-image {
    width: 300px;
    height: 200px;
    object-fit: cover;
    border-radius: 12px;
  }
  
  .hotel-details p {
    margin: 5px 0;
  }
  
  .loading {
    text-align: center;
    margin-top: 100px;
    font-size: 1.2rem;
  }
  </style>
  