<template>
  <header class="main-header">
    <div class="content-wrapper">
      <router-link :to="logoLink" class="logo">
        <div class="logo-wrapper">
          <h1>쉼, 한국</h1>
          <p v-if="authStore.userRole === 'ROLE_ADMIN'" class="admin-mode">사이트 관리자</p>
        </div>
      </router-link>
      <nav class="main-nav">
        <template v-if="authStore.userRole === 'ROLE_ADMIN'">
          <router-link to="/">사이트 메인페이지</router-link>
        </template>
        <template v-else>
          <router-link to="/">홈</router-link>
          <router-link to="/accommodations">지역</router-link>
          <router-link to="/landmarks">랜드마크</router-link>
        </template>
      </nav>
      <div class="user-nav">
        <template v-if="authStore.userRole !== 'ROLE_ADMIN'">
          <router-link :to="authStore.isLoggedIn ? '/mypage' : '/booking-check'" class="nav-link">예약확인</router-link>
          <router-link :to="authStore.isLoggedIn ? '/wishlist' : '/login'" class="nav-link favorites">❤️ 찜하기</router-link>
          <div class="nav-divider"></div>
        </template>

        <template v-if="authStore.isLoggedIn">
          <router-link
            :to="authStore.userRole === 'ROLE_ADMIN' ? '/admin' : '/mypage'"
            class="nav-link-wrapper"
            :title="authStore.userRole === 'ROLE_ADMIN' ? '관리자 페이지로 이동' : ''"
          >
              <span class="nav-link">안녕하세요, {{ authStore.userName }}님</span>
          </router-link>
          <button class="logout-btn" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">로그인</router-link>
          <router-link to="/register" class="signup-btn">회원가입</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const logoLink = computed(() => {
  return authStore.userRole === 'ROLE_ADMIN' ? '/admin' : '/';
});

const handleLogout = () => {
  authStore.logout();
  router.push('/');
};
</script>

<style scoped>
  /* [추가] '나눔명조' 폰트 import */
  @import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');
  @import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap');


  .content-wrapper {
      max-width: 100%;
      padding: 0 5%;
      box-sizing: border-box;
  }
  .main-header {
    height: 80px;
    display: flex;
    align-items: center;
    position: sticky;
    top: 0;
    z-index: 990;
    border-bottom: 1px solid #E5E5E5;
    background-color: #FFFFFF;
    transition: background-color 0.3s ease;
  }
  .main-header:hover {
    background-color: #f5f5f5;
  }
  .main-header .content-wrapper {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    align-items: center;
    width: 100%;
  }
  .logo {
    justify-self: start;
    text-decoration: none;
  }
  .main-nav { justify-self: center; }
  .user-nav { justify-self: end; }

  .logo-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .logo h1 {
    font-family: 'Nanum Myeongjo', serif;
    font-size: 1.8rem;
    font-weight: 700;
    letter-spacing: 2px;
    color: #222222;
    margin: 0;
  }
  
  .admin-mode {
    background-color: #4F46E5;
    color: white;
    padding: 4px 8px;
    border-radius: 5px;
    font-size: 0.8rem;
    font-family: 'Noto Sans KR', sans-serif;
    font-weight: 500;
    display: inline-block;
    margin: 0;
  }

  .main-nav {
    display: flex;
    align-items: center;
    gap: 1.5rem;
  }
  .main-nav a {
    text-decoration: none;
    color: #222222;
    font-weight: 500;
    padding: 10px 15px;
    border-radius: 8px;
    transition: background-color 0.3s ease;
  }
  .main-nav a:hover {
    background-color: #e9e9e9;
  }
  /* [추가] 현재 활성화된 메뉴에 시각적 효과를 주는 스타일 */
  .main-nav a.router-link-exact-active {
    color: #0264FF; /* 파란색으로 강조 */
    font-weight: 700;
  }

  .user-nav {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  .user-nav .nav-link {
    color: #222222;
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 8px;
    transition: background-color 0.3s ease;
  }
  .user-nav .nav-link:hover {
    background-color: #f0f0f0;
  }
  .nav-divider {
    width: 1px;
    height: 16px;
    background-color: #E5E5E5;
  }
  .signup-btn {
    background-color: #222222;
    color: #FFFFFF;
    border: none;
    border-radius: 30px;
    padding: 10px 20px;
    font-weight: 700;
    font-size: 0.9rem;
    cursor: pointer;
    transition: opacity 0.3s ease;
    text-decoration: none;
    display: inline-block;
  }
  .signup-btn:hover {
    opacity: 0.8;
  }
  
  /* [추가] 로그아웃 버튼 스타일 */
  .logout-btn {
    background-color: #f8f9fa;
    color: #495057;
    border: 1px solid #dee2e6;
    border-radius: 30px;
    padding: 10px 20px;
    font-weight: 700;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .logout-btn:hover {
    background-color: #e9ecef;
    border-color: #adb5bd;
    color: #212529;
  }
</style>