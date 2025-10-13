<template>
  <HeaderComponent />
  
  <router-view></router-view>

  <FooterComponent />
</template>

<script setup>
import HeaderComponent from './components/HeaderComponent.vue';
import FooterComponent from './components/FooterComponent.vue';

import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth'; // auth.js 스토어 경로

const authStore = useAuthStore();

// onMounted는 App.vue 컴포넌트가 처음 화면에 그려질 때 딱 한 번 실행됩니다.
onMounted(async () => {
  // 앱이 시작되면, localStorage의 토큰을 확인하고
  // 유효한 경우 서버에서 사용자 정보를 가져와 스토어를 채우는 액션을 호출합니다.
  await authStore.checkLoginStatus();
});
</script>

<style>
@import '@/assets/main.css';
</style>