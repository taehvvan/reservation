<template>
  <div class="success-container">
    <div class="success-card">
      <div class="success-icon">
        <span>🎉</span>
      </div>
      <h1 class="title">환영합니다!</h1>
      <p class="message">
        <strong>{{ name }}님</strong>, 쉼, 한국의 회원이 되신 것을 진심으로 축하합니다.
        <br>이제 '쉼, 한국'의 모든 서비스를 이용하고 특별한 혜택을 받으실 수 있습니다.
        <br>가입 축하 10% 할인 쿠폰이 발급되었습니다. 지금 바로 확인해보세요!
      </p>
      <div class="action-buttons">
        <router-link to="/login" class="btn-primary">로그인 페이지로 이동</router-link>
        <router-link to="/" class="btn-secondary">메인으로 가기</router-link>
      </div>
    </div>
  </div>

</template>

<script setup>
const props = defineProps({
  name: {
    type: String,
    default: '고객'
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;800&display=swap');

.success-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 80px); /* 80px은 헤더 높이만큼 뺀 값 */
  background-color: #F9F9F9;
  font-family: 'Noto Sans KR', sans-serif;
  padding: 40px 20px;
  box-sizing: border-box;
}

.success-card {
  background-color: #fff;
  padding: 50px 60px;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
  max-width: 600px;
  width: 100%;
}

.success-icon {
  font-size: 4rem;
  line-height: 1;
  margin-bottom: 20px;
  display: inline-block;
}

.title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #2C3E50;
  margin: 0 0 15px 0;
}

.message {
  font-size: 1.1rem;
  color: #555;
  line-height: 1.8;
  margin-bottom: 40px;
}

.message strong {
  color: #007bff;
  font-weight: 700;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.action-buttons a {
  padding: 14px 28px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  border: 1px solid transparent;
  transition: all 0.2s ease-in-out;
}

.btn-primary {
  background-color: #0A2A66;
  color: #fff;
  border-color: #0A2A66;
}

.btn-primary:hover {
  background-color: #082255;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.btn-secondary {
  background-color: #fff;
  color: #555;
  border-color: #ddd;
}

.btn-secondary:hover {
  background-color: #f7f7f7;
  border-color: #ccc;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
</style>