<template>
  <footer class="main-footer">
    <div class="content-wrapper">
      <div class="footer-content">
        <div class="footer-info">
          <h3 class="footer-logo">쉼, 한국</h3>
          <p>
            (주)쉼 코리아 | 대표: 홍길동 | 사업자등록번호: 123-45-67890<br>
            주소: 서울특별시 중구 청계천로 40 (한국관광공사)<br>
            고객센터: 1588-0000 | 이메일: contact@shimkorea.com
          </p>
          <p class="copyright">&copy; 2025 Shim, Korea. All rights reserved.</p>
        </div>
        <div class="footer-links">
          <div class="legal-links">
            <router-link to="/terms">이용약관</router-link>
            <router-link to="/privacy"><strong>개인정보 처리방침</strong></router-link>
            <router-link to="/inquiry/create">문의하기</router-link>
            <router-link to="/inquiries/board">문의 목록</router-link>
          </div>
          
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 로직 없음
</script>

<style scoped>
/* ======= 핵심: position 제거 ======= */
.main-footer {
background-color: #F8F8F8;
padding: 50px 0;
border-top: 1px solid #E5E5E5;
color: #666;
font-size: 0.9rem;
}

/* 상위(App.vue 등)에서 아래처럼 설정
.page-wrapper {
display: flex;
flex-direction: column;
min-height: 100vh;
}
.page-content {
flex: 1;
}
==================================== */

.content-wrapper {
max-width: 1280px;
margin: 0 auto;
padding: 0 20px;
}
.footer-content {
display: flex;
justify-content: space-between;
align-items: flex-start;
text-align: left;
}
.footer-logo {
font-size: 1.5rem;
font-weight: 700;
color: #333;
margin: 0 0 15px 0;
}
.footer-info p {
margin: 5px 0;
line-height: 1.7;
}
.copyright {
margin-top: 20px;
font-size: 0.85rem;
color: #999;
}
.footer-links { text-align: right; }
.legal-links { margin-bottom: 20px; }
.legal-links a {
text-decoration: none;
color: #555;
margin-left: 20px;
}
.legal-links a:hover { text-decoration: underline; }
.social-links {
display: flex;
gap: 15px;
justify-content: flex-end;
}
.social-links a {
width: 40px; height: 40px;
border: 1px solid #ddd;
border-radius: 50%;
display: flex; align-items: center; justify-content: center;
}
.social-links a:hover { background-color: #eee; }
.social-links span {
width: 20px; height: 20px; background-color: #555;
-webkit-mask-size: contain; mask-size: contain;
-webkit-mask-repeat: no-repeat; mask-repeat: no-repeat;
-webkit-mask-position: center; mask-position: center;
}
.icon-instagram { /* ...생략... */ }
.icon-facebook  { /* ...생략... */ }
.icon-blog      { /* ...생략... */ }
</style>