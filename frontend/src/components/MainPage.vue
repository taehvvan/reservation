<template>
  <div class="main-container">
    <main>
      <!-- 히어로 배너 (큰 배경) -->
      <section
        class="main-showcase hero-banner"
        :style="{ backgroundImage: `url(${heroBg})` }"
      >
        <!-- 배너 하단에 겹치는 검색바 -->
        <div class="search-floating">
          <div class="search-surface">
            <SearchBar />
          </div>
        </div>

        <!-- (선택) 배너 안쪽 하단 컨트롤 자리에 뭔가 둘 경우 사용 -->
        <div class="slider-controls-wrapper">
          <div class="slider-controls">
            <div class="swiper-pagination-custom"></div>
            <div class="swiper-navigation">
              <div class="swiper-button-prev-custom"></div>
              <div class="swiper-button-next-custom"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 국내 인기 여행지 -->
      <section class="recommendations-section">
        <div class="content-wrapper">
          <div class="section-header">
            <h3>국내 인기 여행지</h3>
          </div>
          <div class="region-grid">
            <div
              v-for="region in recommendedRegions"
              :key="region.name"
              class="region-item"
            >
              <router-link :to="getSearchLink(region.name)" class="region-card">
                <img :src="region.image" :alt="region.name" class="region-card-image" />
                <div class="region-card-overlay"></div>
              </router-link>
              <h4 class="region-name">{{ region.name }}</h4>
            </div>
          </div>
        </div>
      </section>

      <!-- 인기 랜드마크 -->
      <section class="landmark-section">
        <div class="content-wrapper">
          <div class="section-header">
            <h3>
              <router-link to="/landmarks" class="more-link">인기 랜드마크 둘러보기</router-link>
            </h3>
            <router-link to="/landmarks" class="more-link">전체보기</router-link>
          </div>

          <swiper
            :modules="[Navigation]"
            :slides-per-view="'auto'"
            :space-between="20"
            :navigation="{ nextEl: '.landmark-button-next', prevEl: '.landmark-button-prev' }"
            class="landmark-slider"
          >
            <swiper-slide v-for="landmark in popularLandmarks" :key="landmark.id">
              <router-link
                :to="{ name: 'LandmarkDetail', params: { id: landmark.id } }"
                class="landmark-card-link"
              >
                <div class="landmark-card">
                  <div class="landmark-image-container">
                    <img :src="landmark.image" :alt="landmark.name" />
                  </div>
                  <div class="landmark-info">
                    <div class="landmark-tags">
                      <span v-for="tag in landmark.tags" :key="tag">{{ tag }}</span>
                    </div>
                    <h4 class="landmark-name">{{ landmark.name }}</h4>
                    <p class="landmark-location">{{ landmark.location }}</p>
                  </div>
                </div>
              </router-link>
            </swiper-slide>
          </swiper>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Navigation } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';

import SearchBar from './SearchBar.vue';

/* === 히어로 배경 이미지 (원하는 이미지로 경로 교체) === */
import heroBg from '@/assets/images/card-main.jpg';

/* === 국내 인기 여행지 카드 이미지 === */
import seuol from '@/assets/images/card-seoul.jpg';
import jejuImage from '@/assets/images/card-jeju.jpg';
import Gangneung from '@/assets/images/card-Gangneung.jpg';
import Sokcho from '@/assets/images/card-Sokcho.jpg';
import Incheon from '@/assets/images/card-Incheon.jpg';
import Busan from '@/assets/images/card-gwangalli-beach.jpg';

/* === 랜드마크 이미지 === */
import gwangallibeachImage from '@/assets/images/card-gwangalli-beach.jpg';
import lotteworldtowerseoulsky from '@/assets/images/card-lotte-world-tower-seoul-sky.jpg';
import sungnyemun from '@/assets/images/card-sungnyemun.jpg';
import ttukseomhangangpark from '@/assets/images/card-ttukseom-hangang-park.jpg';
import yisunsinstatue from '@/assets/images/card-yi-sun-sin-statue.jpg';

/* 날짜 포맷 & 검색 링크 */
const formatDate = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};
const getSearchLink = (location) => { 
  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);
  return {
    name: 'SearchResult',
    query: {
      region: location,
      startDate: formatDate(today),
      endDate: formatDate(tomorrow),
    }
  };
};

/* 데이터 */
const recommendedRegions = ref([
  { name: '서울', image: seuol },
  { name: '강릉', image: Gangneung },
  { name: '인천', image: Incheon },
  { name: '부산', image: Busan },
  { name: '속초', image: Sokcho },
  { name: '제주', image: jejuImage },
]);

const popularLandmarks = ref([
  { id: 1, name: '뚝섬한강공원', location: '서울특별시 광진구', image: ttukseomhangangpark, tags: ['#고궁', '#역사'] },
  { id: 2, name: '숭례문', location: '서울특별시 중구', image: sungnyemun,  tags: ['#자연', '#오름'] },
  { id: 3, name: '충무공 이순신 동상', location: '서울특별시 종로구', image: yisunsinstatue, tags: ['#한옥', '#문화'] },
  { id: 7, name: '롯데월드타워 서울스카이', location: '서울특별시 송파구', image: lotteworldtowerseoulsky, tags: ['#마을', '#예술'] },
  { id: 8, name: '광안리해수욕장', location: '부산광역시 수영구', image: gwangallibeachImage, tags: ['#유적', '#신라'] },
]);
</script>

<style scoped>
/* ===== 공통 레이아웃 ===== */
.main-container { background-color: #fff; }
.content-wrapper { max-width: 100%; padding: 0 5%; box-sizing: border-box; }

/* ===== 히어로(큰 배경) ===== */
.main-showcase.hero-banner{
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;

  height: 480px;
  border-bottom-left-radius: 28px;
  border-bottom-right-radius: 28px;
  overflow: hidden;

  position: relative;
  /* margin-top: 56px; */ /* 헤더 높이만큼 밀어내던 코드를 주석 처리 또는 삭제 */
}

/* 배너 상단 그라데이션(선택) */
.main-showcase.hero-banner::after{
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0,0,0,0.18) 0%,
    rgba(0,0,0,0.08) 35%,
    rgba(0,0,0,0) 60%
  );
  pointer-events: none;
}

/* 배너 안쪽 하단에 겹치는 검색바 */
.search-floating{
  position: absolute;
  left: 50%;
  bottom: 24px;                  /* 겹치는 간격(16~40px 권장) */
  transform: translateX(-50%);
  z-index: 5;
  width: min(1100px, calc(100% - 40px));
}

/* 검색바 뒤 회색 카드 */
.search-surface{
  background: #f5f6f8;
  border: 1px solid #e7e9ee;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 18px 40px rgba(0,0,0,.08);
}
/* SearchBar 자체가 이미 카드라면 그림자 중복 방지 */
.search-surface .main-search-bar{ box-shadow: none; }

/* (선택) 배너 안쪽 하단 컨트롤 자리 */
.slider-controls-wrapper{
  position: absolute;
  bottom: 24px;
  left: 0;
  width: 100%;
  z-index: 2;
}
.slider-controls{
  display: flex;
  align-items: center;
  gap: 20px;
  padding-left: 5%;
}
.swiper-pagination-custom{ width: auto; font-size: 1rem; }
.swiper-navigation{ display: flex; gap: 1rem; }
.swiper-button-prev-custom::after,
.swiper-button-next-custom::after{ font-size: 1.5rem; color: #222; }


/* ===== 국내 인기 여행지 ===== */
.recommendations-section { padding: 64px 0 80px; }
.region-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 25px; }
.region-item { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.region-card{
  position: relative; overflow: hidden; border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
  cursor: pointer; transition: transform .3s ease, box-shadow .3s ease;
  display: block; text-decoration: none; width: 100%;
}
.region-card:hover { transform: translateY(-8px); box-shadow: 0 12px 25px rgba(0,0,0,0.12); }
.region-card-image{ width: 100%; height: 100%; object-fit: cover; transition: transform .4s ease; aspect-ratio: 3/4; }
.region-card:hover .region-card-image{ transform: scale(1.1); }
.region-card-overlay{ position:absolute; inset:0; background:transparent; transition: background-color .3s ease; }
.region-card:hover .region-card-overlay{ background: rgba(0,0,0,.1); }
.region-name{ font-size: 1.2rem; font-weight: 600; color: #333; }

.section-header{ display:flex; justify-content:space-between; align-items:center; margin-bottom:30px; }
.section-header h3{ font-size:1.8rem; font-weight:700; margin:0; }
.more-link {
  text-decoration: none; /* 밑줄 제거 */
  color: #222; /* 텍스트 색상을 검은색 계열(#222)로 변경 */
  font-weight: 500;
}

/* ===== 랜드마크 슬라이더 ===== */
.landmark-section { padding: 80px 0; position: relative; }
.landmark-slider .swiper-slide { width: 280px; }
.landmark-card{ background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 4px 15px rgba(0,0,0,0.05); transition:transform .3s, box-shadow .3s; }
.landmark-card:hover{ transform:translateY(-8px); box-shadow:0 12px 25px rgba(0,0,0,0.1); }
.landmark-image-container{ width:100%; height:200px; overflow:hidden; }
.landmark-image-container img{ width:100%; height:100%; object-fit:cover; }
.landmark-info{ padding:20px; }
.landmark-tags span{ display:inline-block; background:#f0f0f0; color:#888; font-size:.8rem; padding:4px 8px; border-radius:4px; margin-right:5px; }
.landmark-name{ font-size:1.2rem; font-weight:700; margin:10px 0 5px; }
.landmark-location{ font-size:.9rem; color:#888; margin:0; }

/* 반응형 */
@media (max-width: 640px){
  .main-showcase.hero-banner{ height: 360px; margin-top: 48px; }
  .search-floating{ bottom: 16px; width: calc(100% - 24px); }
  .search-surface{ padding: 12px; border-radius: 14px; }
}
</style>
