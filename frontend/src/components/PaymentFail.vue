<template>
    <div class="payment-success-page">
      <div class="content-wrapper">
        <h1>결제가 실패하였습니다.</h1>
        <div class="button-group">
          <button class="btn-home" @click="goHome">홈으로 돌아가기</button>
          <button class="btn-bookings" @click="goToBookings">예약 내역 확인하기</button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '../stores/auth';

    const router = useRouter();
    const authStore = useAuthStore();
  
  const goHome = () => {
    router.push('/')
  }
  
  const goToBookings = () => {
    if (authStore.isLoggedIn) {
      router.push('/mypage') // 회원이면 마이페이지
    } else {
      router.push('/booking-check') // 비회원이면 예약 조회 페이지
    }
  }
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
    max-width: 500px;
    width: 100%;
  }
  
  h1 {
    font-size: 1.8rem;
    margin-bottom: 20px;
    color: #0A2A66;
  }
  
  p {
    font-size: 1rem;
    margin-bottom: 30px;
    color: #555;
  }
  
  .button-group {
    display: flex;
    justify-content: center;
    gap: 15px;
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
  
  .btn-home {
    background-color: #0A2A66;
    color: #fff;
  }
  
  .btn-home:hover {
    background-color: #09305a;
  }
  
  .btn-bookings {
    background-color: #fff;
    color: #0A2A66;
    border: 2px solid #0A2A66;
  }
  
  .btn-bookings:hover {
    background-color: #0A2A66;
    color: #fff;
  }
  </style>
  