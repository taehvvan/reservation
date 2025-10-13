<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-header">
        <router-link to="/" class="logo">쉼, 한국</router-link>
      </div>
      <form @submit.prevent="handleEmailLogin">
        <div class="input-group">
          <input type="email" id="email" v-model="email" placeholder="이메일 주소" required>
        </div>
        <div class="input-group">
          <input type="password" id="password" v-model="password" placeholder="비밀번호" required>
        </div>
        <button type="submit" class="btn-login-email">로그인</button>
      </form>

      <div class="extra-links">
        <router-link to="/password-reset">비밀번호 찾기</router-link>
        <span class="divider">|</span>
        <router-link to="/register">회원가입</router-link>
      </div>

      <div class="separator"><span>SNS 계정으로 로그인</span></div>

      <div class="social-login">
        <button type="button" class="btn-social kakao" @click="handleKakaoLogin">
          <svg viewBox="0 0 32 32"><path fill-rule="evenodd" clip-rule="evenodd" d="M16 4.66c-6.26 0-11.34 4.41-11.34 9.85C4.66 20.3 9.74 24.7 16 24.7c1.83 0 3.55-.4 5.1-1.12l3.43 3.42c.45.44 1.12.33 1.45-.19.33-.52.2-1.2-.2-1.64l-3.2-3.21c2.6-1.9 4.32-4.95 4.32-8.42 0-5.44-5.08-9.85-11.34-9.85z"></path></svg>
        </button>
        <button type="button" class="btn-social google" @click="handleGoogleLogin">
          <svg viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"></path><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"></path><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"></path><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"></path><path d="M1 1h22v22H1z" fill="none"></path></svg>
        </button>
      </div>

      <div class="manager-login-link">
        <router-link to="/manager-login">호텔 매니저이신가요?</router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const authStore = useAuthStore();

const router = useRouter();

// [수정] 스토어의 login 액션을 호출하도록 변경
const handleEmailLogin = async () => {
  const trimmedEmail = email.value.trim();
  const trimmedPassword = password.value.trim();

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(trimmedEmail)) {
    alert('올바른 이메일 주소를 입력해주세요.');
    return;
  }

  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  if (!passwordRegex.test(trimmedPassword)) {
    alert('비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.');
    return;
  }

  try {
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/auth/login`, {
      email: trimmedEmail,
      password: trimmedPassword
    });

    const accessToken = response.data.accessToken;
    const refreshToken = response.data.refreshToken;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    console.log('로그인 성공, Access Token:', accessToken);
    console.log('로그인 성공, Refresh Token:', refreshToken);

    // fetchUserInfo 호출
    await authStore.fetchUserInfo(accessToken); 

    if (authStore.userRole === 'ROLE_USER') {
      router.push('/');
    } else if (authStore.userRole === 'ROLE_MANAGER') {
      router.push('/manager');
    } else if (authStore.userRole === 'ROLE_ADMIN') {
      router.push('/admin');
    } else {
      router.push('/');
    }


  } catch (error) {
    // 에러 발생 시 auth.js에서 alert를 이미 호출하므로 추가 작업 불필요
    console.log("로그인 컴포넌트에서 에러 확인");
    console.log(error);
  }
};

const handleKakaoLogin = () => { window.location.href = `${import.meta.env.VITE_APP_API_URL}/api/kakao/login`; };
const handleGoogleLogin = () => { window.location.href = `${import.meta.env.VITE_APP_API_URL}/api/google/login`; };
</script>


<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 20px;
  box-sizing: border-box;
}

.login-wrapper {
  width: 100%;
  max-width: 380px;
  text-align: center;
}

.login-header {
  margin-bottom: 40px;
}

.login-header .logo {
  font-family: 'Nanum Myeongjo', serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  text-decoration: none;
}

.input-group {
  margin-bottom: 15px;
}

.input-group input {
  width: 100%;
  padding: 14px 20px;
  font-size: 1rem;
  border: 1px solid #DCDCDC;
  border-radius: 8px;
  box-sizing: border-box;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
}
.input-group input:focus {
  outline: none;
  border-color: #A0A0A0;
}

.btn-login-email {
  width: 100%;
  padding: 14px;
  margin-top: 10px;
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  background-color: #333;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.extra-links {
  margin-top: 20px;
  font-size: 0.9rem;
}
.extra-links a {
  color: #555;
  text-decoration: none;
}
.extra-links .divider {
  margin: 0 10px;
  color: #ccc;
}

.separator {
  display: flex;
  align-items: center;
  text-align: center;
  color: #aaa;
  margin: 40px 0 20px 0;
}
.separator::before,
.separator::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #eee;
}
.separator span {
  padding: 0 15px;
  font-size: 0.9rem;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-social {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 0;
  cursor: pointer;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
  align-items: center;
}
.btn-social svg {
  width: 24px;
  height: 24px;
}
.btn-social.kakao svg {
  width: 28px;
  height: 28px;
}

.manager-login-link {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.manager-login-link a {
  font-size: 0.95rem;
  font-weight: 500;
  color: #555;
  text-decoration: none;
}
.manager-login-link a:hover {
  text-decoration: underline;
}
</style>