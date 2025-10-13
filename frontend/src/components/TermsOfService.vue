<template>
    <div class="legal-page-container">
      <div class="content-wrapper">
        <header class="legal-header">
          <h1>이용약관</h1>
          <p class="last-updated">최종 개정일: 2025년 9월 11일</p>
        </header>
  
        <section class="legal-section">
          <h2 class="section-title">제1조 (목적)</h2>
          <p>
            이 약관은 (주)쉼 코리아(이하 "회사")가 운영하는 "쉼, 한국" 웹사이트 및 관련 서비스(이하 "서비스")를 이용함에 있어 회사와 회원의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.
          </p>
        </section>
  
        <section class="legal-section">
          <h2 class="section-title">제2조 (용어의 정의)</h2>
          <p>
            ① "서비스"라 함은 회사가 제공하는 숙박, 관광지 예약 및 정보 제공 서비스를 의미합니다.
          </p>
          <p>
            ② "회원"이라 함은 본 약관에 동의하고 회사와 이용계약을 체결하여 회사가 제공하는 서비스를 이용하는 고객을 의미합니다.
          </p>
        </section>
  
        <section class="legal-section">
          <h2 class="section-title">제3조 (약관의 효력 및 변경)</h2>
          <p>
            ① 이 약관은 서비스 화면에 공지되거나 기타의 방법으로 회원에게 공지함으로써 효력이 발생합니다.
          </p>
          <p>
            ② 회사는 필요한 경우 관련 법령을 위배하지 않는 범위 내에서 본 약관을 변경할 수 있으며, 변경된 약관은 제1항과 동일한 방법으로 공지함으로써 효력이 발생합니다.
          </p>
        </section>
        
        <section class="legal-section">
          <h2 class="section-title">제4조 (회원의 의무)</h2>
          <p>
            ① 회원은 관계 법령, 이 약관의 규정, 이용안내 및 서비스와 관련하여 공지한 주의사항 등을 준수하여야 하며, 기타 회사의 업무에 방해되는 행위를 하여서는 안 됩니다.
          </p>
        </section>
        
      </div>
    </div>
  </template>
  
  <script setup>
  // 이 컴포넌트는 특별한 로직이 필요 없습니다.
  </script>
  
  <style scoped>
  @import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@400;700;800&family=Noto+Sans+KR:wght@300;400;500;700&display=swap');
  
  .legal-page-container {
    max-width: 900px;
    margin: 80px auto;
    padding: 0 20px;
    font-family: 'Noto Sans KR', sans-serif;
    color: #333;
  }
  .legal-header {
    border-bottom: 2px solid #333;
    padding-bottom: 20px;
    margin-bottom: 40px;
  }
  .legal-header h1 {
    font-family: 'Nanum Myeongjo', serif;
    font-size: 2.5rem;
    font-weight: 800;
    margin: 0;
  }
  .last-updated {
    font-size: 0.9rem;
    color: #666;
    margin-top: 10px;
  }
  .legal-section {
    margin-bottom: 40px;
  }
  .section-title {
    font-family: 'Nanum Myeongjo', serif;
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0 0 15px 0;
    color: #222;
  }
  .legal-section p {
    line-height: 1.8;
    font-size: 1rem;
    color: #555;
    margin: 10px 0;
  }
  strong {
    font-weight: 700;
    color: #222;
  }
  </style>