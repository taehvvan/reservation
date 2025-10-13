<template>
    <div class="list-page-container">
      <div class="content-wrapper">
        <header class="page-header">
          <h1>우리의 보물, 세계유산</h1>
          <p>시간이 빚어낸 대한민국의 위대한 유산을 만나보세요.</p>
        </header>
  
        <div class="filters-and-sort">
          <div class="filters">
            <button class="filter-btn">지역 전체</button>
            <button class="filter-btn">유형 전체</button>
          </div>
          <div class="sort-options">
            <select>
              <option>추천순</option>
              <option>이름순</option>
            </select>
          </div>
        </div>
  
        <div class="heritage-grid">
          <div v-for="item in allHeritageItems" :key="item.id" class="heritage-card">
            <div class="heritage-image-container">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="heritage-info">
              <span class="heritage-category">{{ item.category }}</span>
              <h4 class="heritage-name">{{ item.name }}</h4>
              <p class="heritage-location">{{ item.location }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  
  // 실제 앱에서는 API를 통해 더 많은 문화재 목록을 불러와야 합니다.
  import gyeongjuImage from '@/assets/images/card-gyeongju.jpg';
  import jejuImage from '@/assets/images/card-jeju.jpg';
  import jeonjuImage from '@/assets/images/card-jeonju.jpg';
  
  const allHeritageItems = ref([
      { id: 1, name: '석굴암과 불국사', location: '경북 경주시', image: gyeongjuImage, category: '세계유산' },
      { id: 2, name: '해인사 장경판전', location: '경남 합천군', image: jejuImage, category: '세계유산' },
      { id: 3, name: '종묘', location: '서울 종로구', image: jeonjuImage, category: '세계유산' },
      { id: 4, name: '창덕궁', location: '서울 종로구', image: gyeongjuImage, category: '세계유산' },
      { id: 5, name: '수원 화성', location: '경기 수원시', image: jeonjuImage, category: '세계유산' },
      { id: 6, name: '고창, 화순, 강화 고인돌 유적', location: '전북, 전남, 인천', image: jejuImage, category: '세계유산' },
      { id: 7, name: '제주 화산섬과 용암 동굴', location: '제주특별자치도', image: gyeongjuImage, category: '세계자연유산' },
      { id: 8, name: '한국의 역사마을: 하회와 양동', location: '경북 안동시, 경주시', image: jeonjuImage, category: '세계유산' },
  ]);
  </script>
  
  <style scoped>
  .list-page-container {
    padding: 60px 0;
  }
  .content-wrapper {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 20px;
  }
  .page-header {
    text-align: center;
    margin-bottom: 50px;
  }
  .page-header h1 {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0 0 10px 0;
  }
  .page-header p {
    font-size: 1.1rem;
    color: #666;
    margin: 0;
  }
  .filters-and-sort {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40px;
  }
  .filters {
    display: flex;
    gap: 10px;
  }
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
  .heritage-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 30px;
  }
  .heritage-card {
    text-decoration: none;
    color: inherit;
    display: block;
  }
  .heritage-card:hover .heritage-name {
    color: #0264FF; /* Blue Accent */
  }
  .heritage-image-container {
    width: 100%;
    height: 280px;
    overflow: hidden;
    border-radius: 12px;
    margin-bottom: 1rem;
    background-color: #f0f0f0;
  }
  .heritage-image-container img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }
  .heritage-card:hover img {
    transform: scale(1.05);
  }
  .heritage-info {
    text-align: left;
  }
  .heritage-category {
    font-size: 0.85rem;
    font-weight: 700;
    color: #0264FF; /* Blue Accent */
    margin-bottom: 8px;
    display: block;
  }
  .heritage-name {
    font-size: 1.2rem;
    font-weight: 700;
    margin: 0 0 5px 0;
    transition: color 0.3s ease;
  }
  .heritage-location {
    font-size: 0.95rem;
    color: #888;
    margin: 0;
  }
  </style>