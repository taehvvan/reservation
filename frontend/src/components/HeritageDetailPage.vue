<template>
    <div v-if="heritage" class="detail-page-container">
      <div class="content-wrapper">
        <main class="main-content-column">
          <nav class="breadcrumbs">
            <router-link to="/">홈</router-link>
            <span>›</span>
            <router-link to="/heritage">문화재</router-link>
            <span>›</span>
            <span>{{ heritage.name }}</span>
          </nav>
  
          <div class="hero-image">
            <img :src="heritage.image" :alt="heritage.name">
          </div>
  
          <div class="info-header">
            <h1>{{ heritage.name }}</h1>
            <p class="location">📍 {{ heritage.location }}</p>
          </div>
  
          <section class="description-section">
            <h2>소개</h2>
            <p>{{ heritage.description }}</p>
          </section>
        </main>
  
        <aside class="sidebar-column">
          <div class="sticky-sidebar">
            <div class="nearby-hotel-card">
              <h3>근처 숙소 찾아보기</h3>
              <p>'{{ heritage.location }}' 근처의 멋진 숙소들을 둘러보세요.</p>
              <router-link 
                :to="{ name: 'SearchResult', query: { destination: heritage.location } }"
                class="btn-find-hotels"
              >
                숙소 검색하기
              </router-link>
            </div>
          </div>
        </aside>
      </div>
    </div>
    <div v-else class="loading-container">
      <p>데이터를 불러오는 중입니다...</p>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRoute } from 'vue-router';
  
  const route = useRoute();
  const heritage = ref(null);
  
  const sampleHeritage = [
    { id: '1', name: '석굴암과 불국사', location: '경북 경주시', image: '/src/assets/images/card-gyeongju.jpg', description: '신라 불교예술의 정수로, 건축, 수리, 기하학, 종교, 예술이 총체적으로 실현된 유산입니다. 통일신라시대의 찬란했던 문화를 엿볼 수 있습니다.' },
    { id: '2', name: '해인사 장경판전', location: '경남 합천군', image: '/src/assets/images/card-jeju.jpg', description: '고려대장경(팔만대장경) 목판을 보관하는 보고로, 자연의 원리를 이용한 보존 과학이 돋보입니다.' },
    { id: '3', name: '종묘', location: '서울 종로구', image: '/src/assets/images/card-jeonju.jpg', description: '조선시대 역대 왕과 왕비의 신위를 모시고 제사를 지내는 유교 사당으로, 제사 의식인 종묘제례와 제례악이 함께 보존되어 가치를 더합니다.' },
  ];
  
  const fetchData = () => {
    const id = route.params.id;
    heritage.value = sampleHeritage.find(item => item.id === id);
  };
  
  onMounted(fetchData);
  watch(() => route.params.id, fetchData);
  </script>
  
  <style scoped>
  /* 랜드마크 상세 페이지와 동일한 스타일을 공유합니다. */
  @import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;800&display=swap');
  .detail-page-container { font-family: 'Noto Sans KR', sans-serif; background-color: #fff; padding: 40px 0; }
  .content-wrapper { max-width: 1200px; margin: 0 auto; padding: 0 20px; display: grid; grid-template-columns: 1fr 350px; gap: 40px; align-items: flex-start; }
  .main-content-column, .sidebar-column { min-width: 0; }
  .breadcrumbs { font-size: 0.9rem; color: #888; margin-bottom: 25px; display: flex; align-items: center; gap: 8px; }
  .breadcrumbs a { color: #888; text-decoration: none; }
  .breadcrumbs a:hover { text-decoration: underline; }
  .breadcrumbs span:last-child { font-weight: 500; color: #333; }
  .hero-image { width: 100%; height: 450px; border-radius: 16px; overflow: hidden; margin-bottom: 30px; }
  .hero-image img { width: 100%; height: 100%; object-fit: cover; }
  .info-header { margin-bottom: 30px; }
  h1 { font-size: 2.8rem; font-weight: 800; margin: 0 0 10px 0; color: #222; }
  .location { font-size: 1.1rem; color: #555; font-weight: 500; }
  .description-section h2 { font-size: 1.6rem; font-weight: 700; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
  .description-section p { font-size: 1.1rem; line-height: 1.8; color: #444; }
  .sticky-sidebar { position: sticky; top: 100px; }
  .nearby-hotel-card { background-color: #F8F9FA; border: 1px solid #E5E5E5; border-radius: 12px; padding: 25px; text-align: center; }
  .nearby-hotel-card h3 { font-size: 1.4rem; margin: 0 0 10px 0; }
  .nearby-hotel-card p { font-size: 1rem; color: #666; margin-bottom: 25px; }
  .btn-find-hotels { display: block; width: 100%; background-color: #0A2A66; color: #fff; border: none; border-radius: 8px; padding: 15px; font-size: 1.1rem; font-weight: 600; cursor: pointer; text-decoration: none; }
  .loading-container { display: flex; justify-content: center; align-items: center; height: 50vh; }
  </style>