<template>
  <div>
    <h2>구글 로그인 처리 중...</h2>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  // 백엔드에서 리디렉션된 URL의 쿼리 파라미터에서 토큰을 추출합니다.
  const accessToken = route.query.access_token;
  const refreshToken = route.query.refresh_token;

  if (accessToken && refreshToken) {
    try {
      // 1. 토큰을 로컬 스토리지에 저장합니다.
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // 2. 사용자 정보를 가져와 스토어에 저장합니다.
      await authStore.fetchUserInfo(accessToken); // store에서 토큰을 자동으로 헤더에 추가하도록 수정되었다고 가정

      // 3. 메인 페이지로 리디렉션합니다.
      router.push({ name: 'MainPage' });

    } catch (error) {
      console.error('로그인 처리 실패:', error);
      alert('로그인 처리 중 오류가 발생했습니다.');
      router.push({ name: 'Login' });
    }
  } else {
    // 토큰이 없는 경우 (로그인 실패 시)
    alert('로그인에 실패했습니다.');
    router.push({ name: 'Login' });
  }
});
</script>