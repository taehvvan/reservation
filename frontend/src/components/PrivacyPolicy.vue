<template>
    <div class="legal-page-container">
      <div class="content-wrapper">
        <header class="legal-header">
          <h1>개인정보 처리방침</h1>
          <p class="last-updated">최종 개정일: 2025년 9월 11일</p>
        </header>
  
        <section class="legal-section">
          <h2 class="section-title">제1조 (개인정보의 수집 및 이용 목적)</h2>
          <p>
            회사는 다음의 목적을 위하여 개인정보를 처리합니다. 처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며, 이용 목적이 변경되는 경우에는 개인정보 보호법 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.
          </p>
          <ul>
            <li>회원 가입 및 관리</li>
            <li>서비스 제공에 따른 계약 이행 및 요금 정산</li>
            <li>마케팅 및 광고에의 활용</li>
          </ul>
        </section>
  
        <section class="legal-section">
          <h2 class="section-title">제2조 (수집하는 개인정보 항목)</h2>
          <p>
            회사는 원활한 서비스 제공을 위해 다음과 같은 최소한의 개인정보를 수집하고 있습니다.
          </p>
          <ul>
            <li>필수항목: 이름, 이메일, 휴대전화번호</li>
            <li>선택항목: 주소, 생년월일</li>
          </ul>
        </section>
        
        <section class="legal-section">
          <h2 class="section-title">제3조 (개인정보의 보유 및 이용 기간)</h2>
          <p>
            회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의 받은 개인정보 보유·이용기간 내에서 개인정보를 처리 및 보유합니다.
          </p>
          <p>
            <strong>보유 기간:</strong> 회원 탈퇴 시까지
          </p>
          <p>
            다만, 관계 법령의 규정에 의하여 보존할 필요가 있는 경우 관계 법령에서 정한 기간 동안 회원정보를 보관합니다.
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
  .legal-section p, .legal-section ul {
    line-height: 1.8;
    font-size: 1rem;
    color: #555;
    margin: 10px 0;
  }
  .legal-section ul {
    padding-left: 20px;
  }
  .legal-section li {
    margin-bottom: 5px;
  }
  strong {
    font-weight: 700;
    color: #222;
  }
  </style>