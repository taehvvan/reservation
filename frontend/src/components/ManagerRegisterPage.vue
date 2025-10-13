<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-header">
        <router-link to="/" class="logo">쉼, 한국</router-link>
        <h2>호텔 매니저 신청</h2>
      </div>

      <form @submit.prevent="handleManagerRegister" class="register-form">
        <div class="form-section-group">
          <p class="group-title">계정 정보</p>
          <div class="input-group">
            <input type="text" v-model="name" placeholder="이름" required>
          </div>
          <div class="input-group email-check-group">
            <input type="email" v-model="email" placeholder="이메일 주소" required :disabled="isEmailVerified">
            <button type="button" class="btn-check-duplicate" @click="checkEmailDuplicate" :disabled="email.length === 0 || isEmailVerified">
              {{ isEmailVerified ? '인증 완료' : '중복 확인' }}
            </button>
          </div>
          <p v-if="emailStatus" class="status-message">{{ emailStatus }}</p>

          <div v-if="!isEmailDuplicate && !isEmailVerified" class="input-group centered-btn-group">
            <button type="button" class="btn-send-code" @click="sendVerificationCode" :disabled="!emailStatus.includes('사용 가능')">
              {{ codeSent ? '재전송' : '인증번호 전송' }}
            </button>
          </div>
          <p v-if="verificationStatus" class="status-message">{{ verificationStatus }}</p>

          <div v-if="codeSent && !isEmailVerified" class="input-group verification-group">
            <input type="text" v-model="verificationCode" placeholder="인증번호" required>
            <button type="button" class="btn-verify-code" @click="verifyCode" :disabled="!verificationCode.length">
              인증 확인
            </button>
          </div>
          <div class="input-group">
            <input type="password" v-model="password" placeholder="비밀번호" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="passwordConfirm" placeholder="비밀번호 확인" required>
          </div>
        </div>

        <div class="form-section-group">
          <p class="group-title">사업자 정보</p>
          <div class="input-group">
            <input type="text" v-model="companyName" placeholder="상호명" required>
          </div>
          <div class="input-group">
            <input type="text" v-model="businessNumber" placeholder="사업자 등록번호" required>
          </div>
        </div>

        <button type="submit" class="btn-register" :disabled="!isEmailVerified">매니저 신청하기</button>
      </form>

      <div class="extra-links">
        <span>이미 계정이 있으신가요?</span>
        <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

// --- 상태 변수들 ---
const name = ref('');
const email = ref('');
const password = ref('');
const passwordConfirm = ref('');
const companyName = ref('');
const businessNumber = ref('');
const verificationCode = ref('');

const isEmailDuplicate = ref(true);
const isEmailVerified = ref(false);
const codeSent = ref(false);

const emailStatus = ref('');
const verificationStatus = ref('');

// --- 이메일 인증 관련 함수들 (일반 회원가입 코드와 동일) ---
const checkEmailDuplicate = async () => {
  const trimmedEmail = email.value.trim();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailRegex.test(trimmedEmail)) {
    emailStatus.value = '올바른 이메일 주소를 입력해주세요.';
    return;
  }

  try {
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/check-email`, { email: trimmedEmail });
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
    await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/send-code`, { email: email.value.trim() });
    codeSent.value = true;
    verificationStatus.value = '인증번호가 전송되었습니다. 3분 이내에 입력해주세요.';
  } catch (error) {
    verificationStatus.value = '인증번호 전송에 실패했습니다. 다시 시도해주세요.';
  }
};

const verifyCode = async () => {
  try {
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/verify-code`, {
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

// --- 최종 매니저 회원가입 처리 함수 ---
const handleManagerRegister = async () => {
  // 1. 입력값 유효성 검사
  const trimmedName = name.value.trim();
  const trimmedEmail = email.value.trim();
  const trimmedPassword = password.value.trim();
  const trimmedPasswordConfirm = passwordConfirm.value.trim();
  const trimmedCompanyName = companyName.value.trim();
  const trimmedBusinessNumber = businessNumber.value.trim();

  // 2. 이름 유효성 검사
  const nameRegex = /^[가-힣a-zA-Z\s]{2,}$/;
  if (!nameRegex.test(trimmedName)) {
    alert('이름은 두 글자 이상의 한글 또는 영문만 가능합니다.');
    return;
  }

  // 3. 비밀번호 유효성 검사
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  if (!passwordRegex.test(trimmedPassword)) {
    alert('비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.');
    return;
  }

  // 4. 비밀번호 일치 확인
  if (trimmedPassword !== trimmedPasswordConfirm) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  // 5. 이메일 인증 완료 여부 확인
  if (!isEmailVerified.value) {
    alert('이메일 인증을 먼저 완료해주세요.');
    return;
  }

  try {
    // 요청할 데이터 준비 (ROLE_MANAGER 추가)
    const data = {
      name: trimmedName,
      email: trimmedEmail,
      password: trimmedPassword,
      companyName: trimmedCompanyName,
      businessNumber: trimmedBusinessNumber,
      role: 'ROLE_MANAGER'
    };

    // 백엔드에 요청 보내기
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/manager-register`, data);

    if (response.data.success) {
      // alert('호텔 매니저 신청이 완료되었습니다! 관리자 승인 후 활동하실 수 있습니다.'); // 이 부분을
      router.push('/manager-pending'); // 이렇게 변경합니다.
    } else {
      alert('매니저 신청에 실패했습니다.');
    }
  } catch (error) {
    const message = error.response?.data?.message || error.message;
    alert('매니저 신청 요청 실패: ' + message);
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 20px;
  box-sizing: border-box;
}

.register-wrapper {
  width: 100%;
  max-width: 420px;
  text-align: center;
  background-color: #fff;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
}

.register-header { margin-bottom: 30px; }
.register-header .logo {
  font-family: 'Nanum Myeongjo', serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  text-decoration: none;
}
.register-header h2 {
  margin: 10px 0 0 0;
  font-size: 1.2rem;
  color: #666;
  font-weight: 500;
}

.form-section-group {
  margin-bottom: 25px;
  text-align: left;
}
.group-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
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
}
.input-group input:focus {
  outline: none;
  border-color: #A0A0A0;
}

.email-check-group,
.verification-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.email-check-group input,
.verification-group input {
  flex-grow: 1;
}

.centered-btn-group {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.btn-check-duplicate,
.btn-send-code,
.btn-verify-code {
  width: 110px;
  padding: 14px 10px;
  border-radius: 8px;
  border: 1px solid #DCDCDC;
  background-color: #fff;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  flex-shrink: 0;
}

.status-message {
  font-size: 0.85rem;
  margin-top: -10px;
  margin-bottom: 10px;
  text-align: left;
  color: #555;
}

.btn-register {
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

.btn-check-duplicate:disabled,
.btn-send-code:disabled,
.btn-verify-code:disabled,
.btn-register:disabled {
  background-color: #f0f0f0;
  cursor: not-allowed;
}

.extra-links {
  margin-top: 20px;
  font-size: 0.9rem;
}
.extra-links a {
  color: #0A2A66;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
}
.extra-links a:hover {
  text-decoration: underline;
}
</style>