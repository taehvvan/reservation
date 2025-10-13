<template>
    <div class="password-reset-container">
      <div class="reset-wrapper">
        <div class="reset-header">
          <router-link to="/" class="logo">쉼, 한국</router-link>
          <h4>비밀번호 찾기</h4>
        </div>
  
            <form @submit.prevent="handlePasswordReset">
        <div class="input-group">
        <label for="email">가입한 이메일 주소</label>
        <input 
            type="email" 
            id="email" 
            v-model="email" 
            placeholder="이메일 주소" 
            :disabled="isEmailVerified" 
            required
        />
        </div>
        
        <div class="auth-group">
        <button 
            type="button" 
            class="btn-send-code" 
            @click="sendVerificationCode"
            :disabled="isEmailVerified || !email"
        >
            인증번호 발송
        </button>
        </div>

        <div v-if="isCodeSent" class="input-group verification-input-group">
        <label for="verificationCode">인증번호</label>
        <input 
            type="text" 
            id="verificationCode" 
            v-model="verificationCode" 
            placeholder="인증번호 6자리" 
            required
        />
        <button 
            type="button" 
            class="btn-verify-code" 
            @click="verifyCode"
            :disabled="!verificationCode"
        >
            확인
        </button>
        </div>

        <div v-if="isEmailVerified" class="password-fields">
        <div class="input-group">
            <label for="newPassword">새 비밀번호</label>
            <input 
            type="password" 
            id="newPassword" 
            v-model="newPassword" 
            placeholder="새 비밀번호 (8자 이상, 영문, 숫자, 특수문자)" 
            required
            />
        </div>
        <div class="input-group">
            <label for="confirmPassword">새 비밀번호 확인</label>
            <input 
            type="password" 
            id="confirmPassword" 
            v-model="confirmPassword" 
            placeholder="새 비밀번호 다시 입력" 
            required
            />
        </div>
        </div>
        
        <button 
        type="submit" 
        class="btn-reset" 
        :disabled="!isEmailVerified || !newPassword || newPassword !== confirmPassword"
        >
        비밀번호 재설정
        </button>
    </form>

    <div class="extra-links">
        <router-link to="/login">로그인 페이지로 돌아가기</router-link>
    </div>
    </div>

  
      
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import axios from 'axios';
  import { useRouter } from 'vue-router';
  
  const email = ref('');
  const verificationCode = ref('');
  const newPassword = ref('');
  const confirmPassword = ref('');
  const resetToken = ref('');
  
  const isCodeSent = ref(false);
  const isEmailVerified = ref(false);
  
  const router = useRouter();
  
  // 인증번호 발송 함수
  const sendVerificationCode = async () => {
    try {
      const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/request-reset-password`, {
        email: email.value
      });
      
        if (response.status === 200) {
            isCodeSent.value = true;
            alert(response.data.message);
        }

        } catch (error) {
            console.error('인증번호 발송 실패:', error);
            alert(error.response.data.message || '인증번호 발송에 실패했습니다.');
        }
    };
  
  // 인증번호 확인 함수
  const verifyCode = async () => {
    try {
      const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/verify-code-for-password-reset`, {
        email: email.value,
        code: verificationCode.value
      });
      
      if (response.status === 200) {
        isEmailVerified.value = true;
        resetToken.value = response.data.resetToken;
        alert(response.data.message);
    } else {
        alert(response.data.message);
    }
    } catch (error) {
      console.error('인증번호 확인 실패:', error);
      alert('인증번호 확인에 실패했습니다.');
    }
  };
  
  // 비밀번호 재설정 함수
  const handlePasswordReset = async () => {
    if (newPassword.value !== confirmPassword.value) {
      alert('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
      return;
    }
    
    // 비밀번호 유효성 검사 (로그인 페이지와 동일)
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(newPassword.value)) {
      alert('비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.');
      return;
    }
    
    try {
      const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/reset-password`, {
        resetToken: resetToken.value,
        newPassword: newPassword.value
      });
  
        if (response.status >= 200 && response.status < 300) {
        alert(response.data.message + ' 로그인 페이지로 이동합니다.');
        router.push('/login');
        } else {
        alert(response.data.message);
        }


    } catch (error) {
    console.error('비밀번호 재설정 실패:', error);
    alert(error.response.data.message || '비밀번호 재설정에 실패했습니다.');
  }
};
  </script>
  
  <style scoped>
  /* 비밀번호 찾기 페이지 전용 스타일 */
  .password-reset-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f8f9fa;
    padding: 20px;
    box-sizing: border-box;
  }
  
  .reset-wrapper {
    width: 100%;
    max-width: 400px;
    text-align: center;
    background-color: #fff;
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  
  .reset-header {
    margin-bottom: 30px;
  }
  
  .reset-header .logo {
    font-family: 'Nanum Myeongjo', serif;
    font-size: 2.5rem;
    font-weight: 700;
    color: #333;
    text-decoration: none;
    display: block;
    margin-bottom: 10px;
  }
  
  .reset-header h4 {
    font-family: 'Noto Sans KR', sans-serif;
    font-size: 1.5rem;
    font-weight: 500;
    color: #555;
    margin: 0;
  }
  
  .input-group {
    margin-bottom: 20px;
    text-align: left;
  }
  
  .input-group label {
    display: block;
    font-size: 0.9rem;
    font-weight: 500;
    color: #666;
    margin-bottom: 8px;
  }
  
  .input-group input {
    width: 100%;
    padding: 12px 15px;
    font-size: 1rem;
    border: 1px solid #DCDCDC;
    border-radius: 8px;
    box-sizing: border-box;
  }
  
  .auth-group {
    margin-top: -10px;
    margin-bottom: 20px;
    text-align: right;
  }
  
  .btn-send-code {
    background-color: #333;
    color: #fff;
    border: none;
    border-radius: 8px;
    padding: 10px 15px;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
  }
  
  .btn-send-code:hover {
    background-color: #000;
  }
  
  .verification-input-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    position: relative;
  }
  
  .verification-input-group input {
    width: calc(100% - 70px);
  }
  
  .btn-verify-code {
    position: absolute;
    right: 0;
    bottom: 0;
    padding: 12px 15px;
    background-color: #4CAF50;
    color: #fff;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
  }
  .btn-verify-code:hover {
    background-color: #45a049;
  }
  .btn-verify-code:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
  
  .password-fields {
    margin-top: 30px;
  }
  
  .btn-reset {
    width: 100%;
    padding: 14px;
    font-size: 1rem;
    font-weight: 700;
    color: #fff;
    background-color: #4A69A1;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    margin-top: 15px;
  }
  .btn-reset:disabled {
    background-color: #A0A0A0;
    cursor: not-allowed;
  }
  
  .extra-links {
    margin-top: 30px;
  }
  .extra-links a {
    color: #555;
    text-decoration: none;
    font-size: 0.9rem;
  }
  .extra-links a:hover {
    text-decoration: underline;
  }
  
  </style>