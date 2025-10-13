<template>
  <div>
    <h2>카카오 로그인 처리 중...</h2>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios'; // axios 라이브러리를 사용하면 fetch보다 편리합니다.
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 컴포넌트가 마운트될 때 실행됩니다.
onMounted(async () => {
  const code = route.query.code;

  if (code) {
    try {
      const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/kakao/callback?code=${code}`);
      //const token = response.data.accessToken;
      const accessToken = response.data.accessToken; 
      const refreshToken = response.data.refreshToken;
      // --- 이 부분이 핵심입니다 ---
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      // loginSuccess는 Pinia 스토어의 액션이어야 합니다.
      await authStore.fetchUserInfo();

      //alert('로그인 성공!');
      router.push({ name: 'MainPage' });
    } catch (error) {
      console.error('로그인 실패:', error);
      alert('로그인에 실패했습니다.');
      router.push({ name: 'Login' });
    }
  } else {
    alert('잘못된 접근입니다.');
    router.push({ name: 'MainPage' });
  }
});
</script>