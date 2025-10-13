<template>
    <div class="pending-container">
      <div class="pending-card">
        <div class="pending-icon">
          <span>⏳</span>
        </div>
        <h1 class="title">신청이 완료되었습니다</h1>
        <p class="message">
          <strong>호텔 매니저 가입 신청</strong>이 정상적으로 접수되었습니다.
          <br>사이트 관리자가 검토 후 승인하면 바로 활동하실 수 있습니다.
        </p>
        <div class="action-buttons">
          <router-link to="/" class="btn-primary">메인 페이지로 돌아가기</router-link>
        </div>
      </div>
    </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;800&display=swap');

.pending-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 80px); /* 헤더 높이 제외 */
  background-color: #F9F9F9;
  font-family: 'Noto Sans KR', sans-serif;
  padding: 40px 20px;
}
.pending-card {
  background-color: #fff;
  padding: 50px 60px;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
  max-width: 600px;
}
.pending-icon {
  font-size: 4rem;
  margin-bottom: 20px;
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
  color: #3498DB;
  font-weight: 700;
}
.btn-primary {
  padding: 14px 28px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  background-color: #0A2A66;
  color: #fff;
  transition: all 0.2s ease-in-out;
}
.btn-primary:hover {
  background-color: #082255;
  transform: translateY(-2px);
}
</style>