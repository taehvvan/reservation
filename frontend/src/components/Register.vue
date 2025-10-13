<template>
  <div class="register-container">
    <div class="form-panel">
      <div class="register-wrapper">
        <div class="register-header">
          <router-link to="/" class="logo">쉼, 한국</router-link>
          <h2>회원가입</h2>
        </div>

        <form @submit.prevent="handleRegister" class="register-form">
          <div class="input-group">
            <input type="text" id="name" v-model="name" placeholder="이름" required>
          </div>

          <div class="input-group email-check-group">
            <input type="email" id="email" v-model="email" placeholder="이메일 주소" required :disabled="isEmailVerified">
            <button type="button" class="btn-check-duplicate" @click="checkEmailDuplicate" :disabled="email.length === 0 || isEmailVerified">
              {{ isEmailVerified ? '인증 완료' : '중복 확인' }}
            </button>
          </div>
          <p v-if="emailStatus" class="status-message">{{ emailStatus }}</p>

          <div v-if="!isEmailDuplicate && !isEmailVerified" class="input-group centered-btn-group">
            <button type="button" class="btn-send-code" @click="sendVerificationCode">
              {{ codeSent ? '재전송' : '인증번호 전송' }}
            </button>
          </div>
          <p v-if="verificationStatus" class="status-message">{{ verificationStatus }}</p>

          <div v-if="codeSent && !isEmailVerified" class="input-group verification-group">
            <input type="text" id="verificationCode" v-model="verificationCode" placeholder="인증번호" required>
            <button type="button" class="btn-verify-code" @click="verifyCode" :disabled="!verificationCode.length">
              인증 확인
            </button>
          </div>

          <div class="input-group">
            <input type="password" id="password" v-model="password" placeholder="비밀번호" required>
          </div>
          <div class="input-group">
            <input type="password" id="passwordConfirm" v-model="passwordConfirm" placeholder="비밀번호 확인" required>
          </div>

          <button type="submit" class="btn-register" :disabled="!isEmailVerified">회원가입</button>
        </form>

        <div class="extra-links">
          <span>이미 계정이 있으신가요?</span>
          <router-link to="/login">로그인</router-link>
        </div>

        <div class="separator"><span>OR</span></div>

        <div class="social-login">
          <button type="button" class="btn-social google" @click="handleGoogleLogin">
            <img src="https://img.icons8.com/color/48/google-logo.png" alt="Google"/>
            Google 계정으로 가입
          </button>
          <button type="button" class="btn-social kakao" @click="handleKakaoLogin">
            <img src="https://www.kakaocorp.com/page/favicon.ico" alt="Kakao"/>
            카카오 계정으로 가입
          </button>
        </div>
        
        <div class="manager-signup">
          <router-link to="/manager-register">호텔 관리자이신가요?</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// --- 모든 스크립트 기능은 이전과 동일하게 유지 ---
const name = ref('');
const email = ref('');
const password = ref('');
const passwordConfirm = ref('');
const verificationCode = ref('');
const isEmailDuplicate = ref(true);
const isEmailVerified = ref(false);
const codeSent = ref(false);
const emailStatus = ref('');
const verificationStatus = ref('');

const checkEmailDuplicate = async () => {
  const trimmedEmail = email.value.trim();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(trimmedEmail)) {
    emailStatus.value = '올바른 이메일 주소를 입력해주세요.';
    return;
  }
  try {
    const response = await apiClient.post('/api/check-email', { email: trimmedEmail });
    isEmailDuplicate.value = response.data.isDuplicate;
    if (response.data.isDuplicate) {
      emailStatus.value = '이미 사용 중인 이메일입니다.';
    } else {
      emailStatus.value = '사용 가능한 이메일입니다.';
    }
  } catch (error) {
    emailStatus.value = '이메일 중복 확인에 실패했습니다.';
  }
};

const sendVerificationCode = async () => {
  try {
    await apiClient.post('/api/send-code', { email: email.value.trim() });
    codeSent.value = true;
    verificationStatus.value = '인증번호가 전송되었습니다. 3분 이내에 입력해주세요.';
  } catch (error) {
    verificationStatus.value = '인증번호 전송에 실패했습니다. 다시 시도해주세요.';
  }
};

const verifyCode = async () => {
  try {
    const response = await apiClient.post('/api/verify-code', {
      email: email.value.trim(),
      code: verificationCode.value.trim()
    });
    if (response.data.message === '이메일 인증이 완료되었습니다.') {
      isEmailVerified.value = true;
      verificationStatus.value = '인증이 완료되었습니다.';
    } else {
      verificationStatus.value = '인증번호가 일치하지 않습니다.';
    }
  } catch (error) {
    verificationStatus.value = error.response?.data?.message || '인증번호 확인에 실패했습니다.';
  }
};

const handleRegister = async () => {
  const trimmedName = name.value.trim();
  const trimmedEmail = email.value.trim();
  const trimmedPassword = password.value.trim();
  const trimmedPasswordConfirm = passwordConfirm.value.trim();
  const nameRegex = /^[가-힣a-zA-Z\s]{2,}$/;
  if (!nameRegex.test(trimmedName)) {
    alert('이름은 두 글자 이상의 한글 또는 영문만 가능합니다.');
    return;
  }
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  if (!passwordRegex.test(trimmedPassword)) {
    alert('비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.');
    return;
  }
  if (trimmedPassword !== trimmedPasswordConfirm) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }
  if (!isEmailVerified.value) {
    alert('이메일 인증을 먼저 완료해주세요.');
    return;
  }
  try {
    const data = { name: trimmedName, email: trimmedEmail, password: trimmedPassword };
    const response = await apiClient.post('/api/register', data);

    if (response.data && response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      await authStore.fetchUserInfo();
      router.push({ name: 'RegisterSuccess', query: { name: name.value } });
    } else {
       alert('회원가입에 실패했습니다. (토큰 수신 실패)');
    }
  } catch (error) {
    const message = error.response?.data?.message || '회원가입 요청에 실패했습니다. 이메일이 중복되었는지 확인해주세요.';
    alert(message);
  }
};

const handleKakaoLogin = () => { window.location.href = `${import.meta.env.VITE_APP_API_URL}/api/kakao/login`; };
const handleGoogleLogin = () => { window.location.href = `${import.meta.env.VITE_APP_API_URL}/api/google/login`; };
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');

/* 전체 레이아웃 */
.register-container {
  display: flex; /* Flexbox로 변경하여 중앙 정렬 용이 */
  justify-content: center; /* 가로 중앙 정렬 */
  align-items: center; /* 세로 중앙 정렬 */
  min-height: 100vh;
  background-color: #f8f9fa; /* 부드러운 배경색 */
  font-family: 'Noto Sans KR', sans-serif;
  padding: 20px; /* 전체적인 패딩 추가 */
  box-sizing: border-box; /* 패딩이 크기에 포함되도록 */
}

/* 폼 패널 (이전 image-panel 대신 메인 컨테이너 역할) */
.form-panel {
  background-color: #ffffff; /* 하얀색 배경 */
  border-radius: 12px; /* 둥근 모서리 */
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08); /* 부드러운 그림자 */
  padding: 40px; /* 내부 패딩 */
  width: 100%;
  max-width: 480px; /* 폼의 최대 너비 증가 */
  box-sizing: border-box;
}

.register-wrapper {
  text-align: center;
}

.register-header { margin-bottom: 35px; }
.register-header .logo {
  font-family: 'Nanum Myeongjo', serif;
  font-size: 2.8rem; /* 로고 크기 증가 */
  color: #333;
  text-decoration: none;
  display: block; /* 블록 요소로 만들어 마진 적용 */
  margin-bottom: 5px;
}
.register-header h2 {
  margin-top: 0;
  font-size: 1.3rem; /* "회원가입" 텍스트 크기 증가 */
  color: #555;
  font-weight: 400;
}

/* 폼 스타일 */
.input-group { margin-bottom: 20px; }
.input-group input {
  width: 100%;
  padding: 15px 20px; /* 패딩 증가 */
  font-size: 1.05rem; /* 폰트 크기 약간 증가 */
  border: 1px solid #e0e0e0;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s, background-color 0.2s;
}
.input-group input:focus {
  outline: none;
  border-color: #326273; /* 더 차분한 푸른색 */
  background-color: #fff;
  box-shadow: 0 0 0 4px rgba(50, 98, 115, 0.1);
}
.email-check-group, .verification-group { display: flex; align-items: center; gap: 10px; }
.email-check-group input, .verification-group input { flex-grow: 1; }
.centered-btn-group { display: flex; justify-content: center; margin-bottom: 15px; }

/* 버튼 스타일 */
.btn-check-duplicate, .btn-send-code, .btn-verify-code {
  padding: 15px 12px; /* 패딩 증가 */
  border-radius: 8px;
  border: 1px solid #dcdcdc;
  background-color: #fff;
  font-size: 0.95rem; /* 폰트 크기 약간 증가 */
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
  min-width: 120px; /* 최소 너비 증가 */
  transition: all 0.2s;
}
.btn-check-duplicate:hover, .btn-send-code:hover, .btn-verify-code:hover {
  background-color: #f0f0f0;
  border-color: #c0c0c0;
}
.status-message {
  font-size: 0.88rem;
  margin-top: -15px; /* 입력 필드와의 간격 조절 */
  margin-bottom: 18px;
  text-align: left;
  padding-left: 5px;
  color: #666;
}
.btn-register {
  width: 100%;
  padding: 17px; /* 패딩 증가 */
  margin-top: 20px; /* 상단 마진 증가 */
  font-size: 1.15rem; /* 폰트 크기 증가 */
  font-weight: 700;
  color: #fff;
  background-color: #326273; /* 차분한 푸른색 */
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-register:hover {
  background-color: #274c5a;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(50, 98, 115, 0.25);
}
.btn-register:disabled,
.btn-check-duplicate:disabled,
.btn-send-code:disabled,
.btn-verify-code:disabled {
  background-color: #e9ecef;
  border-color: #e9ecef;
  color: #adb5bd;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.extra-links { margin-top: 30px; font-size: 1rem; color: #666; }
.extra-links a { color: #326273; text-decoration: none; font-weight: 600; margin-left: 8px; }
.extra-links a:hover { text-decoration: underline; }

.separator {
  display: flex; align-items: center; text-align: center;
  color: #ccc; margin: 35px 0; /* 마진 증가 */
}
.separator::before, .separator::after {
  content: ''; flex: 1; border-bottom: 1px solid #eee;
}
.separator span { padding: 0 15px; font-size: 0.9rem; font-weight: 500; color: #888;}

.social-login { display: flex; flex-direction: column; gap: 12px; /* 간격 증가 */ }
.btn-social {
  display: flex; align-items: center; justify-content: center; gap: 12px; /* 간격 증가 */
  width: 100%; padding: 14px; border-radius: 8px; border: 1px solid #ddd;
  background-color: #fff; font-size: 1.05rem; font-weight: 500; cursor: pointer;
  transition: all 0.2s;
}
.btn-social:hover { border-color: #a0a0a0; }
.btn-social img { width: 24px; height: 24px; }
.btn-social.kakao { background-color: #FEE500; border-color: #FEE500; color: #3C1E1E; }
.btn-social.kakao:hover { background-color: #FCD900; border-color: #FCD900; }

.manager-signup {
  text-align: center; /* 관리자 링크 중앙 정렬 */
  margin-top: 30px; /* 상단 마진 증가 */
}
.manager-signup a {
  color: #777; font-weight: 500;
  text-decoration: none; font-size: 0.98rem;
}
.manager-signup a:hover { text-decoration: underline; color: #326273; }
</style>  