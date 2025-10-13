<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-header">
        <router-link to="/" class="logo">쉼, 한국</router-link>
        <h2>호텔 매니저 로그인</h2>
      </div>
      <form @submit.prevent="handleManagerLogin">
        <div class="input-group">
          <input type="email" id="email" v-model="email" placeholder="이메일 주소" required>
        </div>
        <div class="input-group">
          <input type="password" id="password" v-model="password" placeholder="비밀번호" required>
        </div>
        <button type="submit" class="btn-login-email">매니저 로그인</button>
      </form>
      <div class="extra-links">
        <router-link to="/password-reset">비밀번호 찾기</router-link>
        <span class="divider">|</span>
        <router-link to="/manager-register">매니저 가입</router-link>
      </div>
      
      <div class="separator"></div>

      <div class="user-login-link">
        <router-link to="/login">일반 회원이신가요?</router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const email = ref('');
const password = ref('');
const authStore = useAuthStore();

// [수정] 스토어의 login 액션을 호출하도록 변경
const handleManagerLogin = async () => {
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
    // [수정] 세 번째 인자로 role 전달
    await authStore.login(trimmedEmail, trimmedPassword, 'ROLE_MANAGER');
  } catch (error) {
    console.log("매니저 로그인 컴포넌트에서 에러 확인");
  }
};
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
  background-color: #fff;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
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
.login-header h2 {
  margin: 10px 0 0 0;
  font-size: 1.2rem;
  color: #666;
  font-weight: 500;
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