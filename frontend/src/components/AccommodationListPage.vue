<!-- AccommodationsListPage.vue -->
<template>
  <div class="list-page-container">
    <div class="content-wrapper">
      <!-- 헤더 -->
      <header class="page-header">
        <h1>여행지를 찾아보세요</h1>
        <p>쉼, 한국이 엄선한 최고의 공간들을 만나보세요.</p>
      </header>

      <!-- 섹션 리스트 (이미지/텍스트 좌우 번갈아) -->
      <div class="feature-list">
        <section
          v-for="(item, idx) in regions"
          :key="item.id"
          class="feature-row"
          :class="{ reverse: idx % 2 === 1 }"
        >
          <!-- 이미지 -->
          <div class="image-col">
            <div class="image-wrap">
              <img :src="item.image" :alt="item.title" />
            </div>
          </div>

          <!-- 텍스트 -->
          <div class="text-col">
            <span class="region-label">{{ item.region }}</span>
            <h3 class="feature-title">{{ item.title }}</h3>
            <p class="feature-desc">{{ item.desc }}</p>

            <div class="tags">
              <span v-for="(t, i) in item.tags" :key="i" class="tag">#{{ t }}</span>
            </div>

            <button class="cta" @click="goToRegion(item)">이 지역 숙소 보기</button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import seuol from '@/assets/images/card-seoul.jpg';
import jejuImage from '@/assets/images/card-jeju.jpg';
import Gangneung from '@/assets/images/card-Gangneung.jpg';
import Sokcho from '@/assets/images/card-Sokcho.jpg';
import Incheon from '@/assets/images/card-Incheon.jpg';
import Busan from '@/assets/images/card-gwangalli-beach.jpg';


const router = useRouter()


const regions = ref([
  {
  
    id: 1,
    region: '서울',
    title: '[서울] 도심 속 고궁 나들이',
    desc: '과거와 현재가 공존하는 서울의 상징. 고즈넉한 궁과 도심의 카페 골목을 함께 즐겨보세요.',
    tags: ['관광지', '대중교통', '서울중부'],
    image: seuol,
    destination: '서울특별시'
  },
  {
    id: 2,
    region: '강릉',
    title: '[강릉] 푸른 바다와 커피 향기',
    desc: '푸른 동해와 바람이 만드는 카페 도시의 매력. 안목 카페거리와 해변 산책로를 걸어보세요.',
    tags: ['오션뷰', '카페거리', '힐링여행'],
    image: Gangneung,
    destination: '강원특별자치도 강릉시'
  },
  {
    id: 3,
    region: '인천',
    title: '[인천] 이국적인 항구 도시의 밤',
    desc: '항구의 조명과 고즈넉한 골목이 만드는 낭만. 차이나타운과 월미도로 한 번에.',
    tags: ['야경', '도시여행', '먹거리'],
    image: Incheon,
    destination: '인천광역시'
  },
  {
    id: 4,
    region: '속초',
    title: '[속초] 설악과 바다가 만나는 곳',
    desc: '장쾌한 설악산 능선과 동해의 푸른 물결을 하루에. 영금정과 중앙시장을 코스로 즐겨보세요.',
    tags: ['설악산', '바다', '시장먹방'],
    image: Sokcho,       // 임시 이미지
    destination: '강원특별자치도 속초시'
  },
  {
    id: 5,
    region: '부산',
    title: '[부산] 감천부터 해운대까지',
    desc: '알록달록 감천 문화마을, 광안대교 야경, 해운대 바다. 낮엔 산책, 밤엔 드라이브.',
    tags: ['해운대', '야경', '먹거리'],
    image: Busan,       // 임시 이미지
    destination: '부산광역시'
  },
  {
    id: 6,
    region: '제주',
    title: '[제주] 바람, 오름, 드라이브',
    desc: '돌담과 올레길, 초록 오름을 따라 섬을 한 바퀴. 성산일출봉부터 협재까지 천천히.',
    tags: ['오름', '드라이브', '힐링'],
    image: jejuImage,       // 임시 이미지
    destination: '제주특별자치도'
  }
])



const goToRegion = (item) => {
  // 라우터가 있다면 검색 페이지로 이동 (프로젝트 라우트 이름 맞게 수정 가능)
  try {
    router.push({ name: 'SearchResult', query: { destination: item.destination || item.region } })
  } catch {
    // 라우트가 없으면 일단 무시
    console.warn('SearchResult 라우트가 없어요. 라우터 이름을 맞춰주세요.')
  }
}
</script>

<style scoped>
/* 페이지 레이아웃 */
.list-page-container { padding: 60px 0; }
.content-wrapper {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 헤더 */
.page-header { text-align: center; margin-bottom: 50px; }
.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 10px;
}
.page-header p { font-size: 1.1rem; color: #666; margin: 0; }

/* 필터/정렬 바 */
.filters-and-sort {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}
.filters { display: flex; gap: 10px; }
.filter-btn {
  background: #f1f1f1;
  border: 1px solid #ddd;
  border-radius: 30px;
  padding: 8px 16px;
  font-size: 0.9rem;
  cursor: pointer;
}
.sort-options select {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 0.9rem;
}

/* 섹션 리스트 (번갈아 배치) */
.feature-list { display: flex; flex-direction: column; gap: 80px; }

.feature-row {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  gap: 48px;
}

.image-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  height: 340px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
}
.image-wrap img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform .5s ease;
}
.image-wrap:hover img { transform: scale(1.04); }
.feature-row { display:flex; align-items:center; gap:48px; }
.feature-row.reverse { flex-direction: row-reverse; }
.image-col { flex: 1.1; }
.text-col  { flex: 0.9; }

/* 텍스트 컬럼 */
.region-label {
  display: inline-block;
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 8px;
}
.feature-title {
  font-size: 1.6rem;
  font-weight: 800;
  margin: 0 0 10px;
}
.feature-desc {
  color: #555;
  line-height: 1.6;
  margin: 0 0 16px;
}
.tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.tag {
  font-size: 0.85rem;
  background: #f5f5f5;
  border: 1px solid #e5e7eb;
  padding: 6px 10px;
  border-radius: 999px;
  color: #374151;
}

/* CTA 버튼 (스샷 느낌의 pill + 진한 버튼) */
.cta {
  display: inline-block;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform .05s ease, opacity .2s ease;
}
.cta:hover { opacity: .9; }
.cta:active { transform: translateY(1px); }
/* 필터 버튼 공통 */
.filter-btn {
  background: #f1f1f1;
  border: 1px solid #ddd;
  border-radius: 30px;
  padding: 8px 16px;
  font-size: 0.9rem;
  cursor: pointer;
}

</style>
