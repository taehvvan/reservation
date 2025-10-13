검색 결과 페이지
<template>
  <div class="page-container">

    <SearchBar />

    <div class="search-result-container">
      <div class="filters-column">
        <div class="filter-header">
          <h4>필터</h4>
          <button class="btn-reset" @click="resetFilters">초기화</button>
        </div>

        <div class="filter-group">
          <h5>호텔 이름 / 주소 검색</h5>
          <input
            type="text"
            v-model="hotelNameSearchQuery"
            class="hotel-search-input"
            placeholder="호텔 이름 또는 주소를 입력하세요"
          />
        </div>

        <div class="filter-group">
          <h5>숙소 유형</h5>
          <div class="type-button-group">
            <button
              v-for="type in types"
              :key="type.value"
              @click="toggleType(type.value)"
              class="type-filter-btn"
              :class="{ active: selectedTypes.includes(type.value) }"
            >
              {{ type.label }}
            </button>
          </div>
        </div>

        <div class="filter-group">
          <h5>가격 (1박 기준)</h5>
          <div class="price-range-slider">
            <div class="slider-track" ref="sliderTrack"></div>
            <input
              type="range"
              class="price-slider-min"
              min="0"
              max="1000000"
              step="10000"
              v-model.number="priceRange.min"
            >
            <input
              type="range"
              class="price-slider-max"
              min="0"
              max="1000000"
              step="10000"
              v-model.number="priceRange.max"
            >
          </div>
          <div class="price-display">
            <span>₩{{ priceRange.min.toLocaleString() }}</span> -
            <span>₩{{ priceRange.max.toLocaleString() }}</span>
          </div>
        </div>

        <div class="filter-group">
          <h5>평점</h5>
          <div class="rating-filter-card">
            <div class="rating-filter-wrapper">
              <button
                class="zero-btn"
                @click="rating = 0"
                :class="{ active: rating === 0 }"
              >
                전체
              </button>

              <div class="rating-filter">
                <button
                  v-for="star in 5"
                  :key="star"
                  @click="rating = star"
                  :class="{ active: rating >= star }"
                >★</button>
              </div>
            </div>
            <span>{{ rating === 0 ? '모든 평점' : rating.toFixed(1) + '점 이상' }}</span>
          </div>
        </div>

        <div class="filter-group">
          <h5>편의시설</h5>
          <div class="checkbox-group">
            <label v-for="item in amenities" :key="item.id" class="checkbox-label">
              <input type="checkbox" v-model="item.selected">
              {{ item.name }}
            </label>
          </div>
        </div>
      </div>

      <div class="results-main-panel">
        <div class="search-summary">
          <h2><strong>'{{ destination }}'</strong> 검색 결과</h2>
          <div class="sort-options">
            <select v-model="sortOption">
              <option value="random">추천순</option>
              <option value="priceAsc">낮은 요금순</option>
              <option value="ratingDesc">사용자 평점순</option>
            </select>
          </div>
        </div>

        <div class="results-list">
          <div v-if="sortedResults.length > 0">
            <router-link
              v-for="item in sortedResults"
              :key="item.hId"
              :to="{ 
                name: 'HotelDetail', 
                params: { id: item.hId }, 
                query: {
                  destination: destination, // 앞 두 단어 처리된 값
                  startDate: checkIn
                    ? checkIn.toISOString().split('T')[0] 
                    : new Date().toISOString().split('T')[0], // 오늘
                  endDate: checkOut
                    ? checkOut.toISOString().split('T')[0] 
                    : (() => {
                        const tomorrow = new Date();
                        tomorrow.setDate(new Date().getDate() + 1);
                        return tomorrow.toISOString().split('T')[0];
                      })(), // 내일
                  rooms: rooms || 1,
                  persons: persons || 2
                }
              }"
              class="result-card"
            >
              <div class="result-card-inner">
                <div class="image-wrapper">
                  <img
                    :src="item.image ? item.image : '/default-hotel.jpg'"
                    :alt="item.hname">
                </div>
                <div class="info-wrapper">
                  <div class="info-header">
                    <div class="info-badges">
                      <span class="item-type">{{ item.type }}</span>
                    </div>
                    <h3>{{ item.hName }}</h3>
                  </div>
                  <div class="rating-section">
                    <div class="rating-card" v-if="item.reviewCount > 0">
                      <span class="score-badge">{{ item.avgScore.toFixed(1) }}</span>
                      <span class="rating-text">{{ getRatingText(item.avgScore) }}</span>
                    </div>
                    <div v-else>
                      <span class="rating-text">아직 리뷰가 없습니다</span>
                    </div>
                  </div>
                  <div class="details-group">
                    <p class="grade">
                      <span class="hotel-grade-stars">{{ '★'.repeat(item.star) }}</span> {{ translateType(item.type) }}
                    </p>
                    <p class="location">
                      <span class="location-icon">📍</span>{{ item.address }}
                    </p>
                    <p class="amenities">
                      <strong>주요 편의시설:</strong>
                      {{
                        item.services?.length
                          ? (() => {
                              const names = item.services.map(s => s.serviceName);
                              if (names.length > 6) {
                                return names.slice(0, 6).join(', ') + ' ...';
                              } else {
                                return names.join(', ');
                              }
                            })()
                          : '정보 없음'
                      }}
                    </p>
                  </div>
                </div>
                <div class="price-wrapper">
                  <button 
                    class="like-button" 
                    @click.prevent="toggleFavorite(item.hId)"
                  >
                    {{ isFavorite(item.hId) ? '❤️' : '♡' }}
                  </button>
                  <div class="final-price-box">
                    <span class="price-label">1박 최저가</span><br>
                    <strong>{{ item.minPrice.toLocaleString() ?? 0 }}원</strong>
                  </div>
                </div>
              </div>
            </router-link>
          </div>
          <div v-else>
            <p>죄송합니다, 검색 조건에 맞는 결과를 찾을 수 없습니다. 검색 조건을 변경 후 다시 조회해 주시기 바랍니다.</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import axios from 'axios';
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import SearchBar from './SearchBar.vue';

const router = useRouter();
const route = useRoute();
const isLoggedIn = ref(!!localStorage.getItem("accessToken"));
const wishlistItems = ref([]);

const destination = ref('');
const checkIn = ref(null);
const checkOut = ref(null);
const rooms = ref(1);
const persons = ref(2);
const sortOption = ref('random'); // 기본 정렬: 랜덤
const rating = ref(0.0);

const searchResults = ref([]);
const randomizedResults = ref([]); // ✅ 최초 로드 시 랜덤 저장

const hotelNameSearchQuery = ref('');
const selectedTypes = ref([]);

// 필터 상태
const typeMap = {
  Hotel: '호텔',
  Motel: '모텔',
  Hanok: '한옥',
  Pension: '펜션/풀빌라',
  Guesthouse: '게스트하우스/비앤비',
  Resort: '리조트'
};

const types = computed(() => {
  const unique = new Set((searchResults.value ?? []).map(item => item.type));
  return [...unique].map(type => ({
    value: type,
    label: typeMap[type] || type
  }));
});

const toggleType = (type) => {
  if (selectedTypes.value.includes(type)) {
    selectedTypes.value = selectedTypes.value.filter(t => t !== type);
  } else {
    selectedTypes.value.push(type);
  }
};

const amenities = ref([]);

watch(searchResults, (newResults) => {
  const allServices = (newResults ?? [])
    .flatMap(hotel => hotel.services?.map(s => s.serviceName) || []);
  const unique = [...new Set(allServices)];

  amenities.value = unique.map((name, idx) => {
    const old = amenities.value.find(a => a.name === name);
    return {
      id: idx + 1,
      name,
      selected: old ? old.selected : false
    };
  });
});

const priceRange = ref({ min: 0, max: 200000 });
const sliderStep = 10000;

watch(searchResults, (newResults) => {
  if (newResults.length > 0) {
    const maxMinPrice = Math.max(...newResults.map(h => h.minPrice));
    if (maxMinPrice > 200000) {
      const adjustedMax = Math.ceil(maxMinPrice / sliderStep) * sliderStep;
      priceRange.value.max = adjustedMax;
    } else {
      priceRange.value.max = 200000;
    }
  }
});

watch(() => priceRange.value.min, (newVal) => {
  if (newVal > priceRange.value.max) {
    priceRange.value.min = priceRange.value.max;
  }
});

watch(() => priceRange.value.max, (newVal) => {
  if (newVal < priceRange.value.min) {
    priceRange.value.max = priceRange.value.min;
  }
});

// 선택된 편의시설
const selectedAmenities = computed(() =>
  amenities.value.filter(a => a.selected).map(a => a.name)
);

// 필터 초기화
const resetFilters = () => {
  selectedTypes.value = [];
  amenities.value.forEach(a => a.selected = false);
  priceRange.value = { min: 0, max: 150000 };
  rating.value = 0.0;
  hotelNameSearchQuery.value = '';
};

// URL 쿼리 로드
const loadSearchQueryFromUrl = () => {
  const query = route.query;
  // region 또는 destination 값 가져오기
  const today = new Date();
  const tomorrow = new Date();
  tomorrow.setDate(today.getDate() + 1);

  let rawDestination = query.region || query.destination || '';

  // 앞의 2단어만 추출
  if (rawDestination) {
    const words = rawDestination.split(/\s+/); // 공백 기준 분리
    rawDestination = words.slice(0, 2).join(' ');
  }

destination.value = rawDestination;
  checkIn.value = query.startDate ? new Date(query.startDate) : today;
  checkOut.value = query.endDate ? new Date(query.endDate) : tomorrow;
  rooms.value = Number(query.rooms) || 1;
  persons.value = Number(query.persons) || 2;
};

const sliderTrack = ref(null);
const sliderMin = 0;
const sliderMax = 1000000;

const updateSliderTrack = () => {
  const minPercent = ((priceRange.value.min - sliderMin) / (sliderMax - sliderMin)) * 100;
  const maxPercent = ((priceRange.value.max - sliderMin) / (sliderMax - sliderMin)) * 100;

  if (sliderTrack.value) {
    sliderTrack.value.style.background = `linear-gradient(
      to right,
      #E0E0E0 0%,
      #E0E0E0 ${minPercent.toFixed(2)}%,
      #007bff ${minPercent.toFixed(2)}%,
      #007bff ${maxPercent.toFixed(2)}%,
      #E0E0E0 ${maxPercent.toFixed(2)}%,
      #E0E0E0 100%
    )`;
  }
};

watch(priceRange, updateSliderTrack, { deep: true });
onMounted(updateSliderTrack);

// 검색 API 호출
const sendSearchRequest = async () => {
  const validStartDate = checkIn.value instanceof Date && !isNaN(checkIn.value);
  const validEndDate = checkOut.value instanceof Date && !isNaN(checkOut.value);

  const requestBody = {
    region: destination.value,
    startDate: validStartDate ? checkIn.value.toISOString().split('T')[0] : null,
    endDate: validEndDate ? checkOut.value.toISOString().split('T')[0] : null,
    numberOfRooms: rooms.value,
    numberOfPeople: persons.value,
  };

  try {
    const response = await fetch('/api/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody),
    });

    if (response.status === 204) {
      searchResults.value = [];
      randomizedResults.value = [];
      return;
    }

    if (response.ok) {
      const data = await response.json();
      searchResults.value = data.map(item => ({
        ...item,
        image: `${import.meta.env.VITE_APP_API_URL}/images/${item.type}/${item.hId}.jpg`
      }));
      // ✅ 최초 검색 시만 랜덤 섞음
      randomizedResults.value = [...searchResults.value].sort(() => Math.random() - 0.5);
    } else {
      console.error('검색 실패:', response.status);
      searchResults.value = [];
      randomizedResults.value = [];
    }
  } catch (error) {
    console.error('API 호출 중 오류:', error);
    searchResults.value = [];
    randomizedResults.value = [];
  }
};

// URL 쿼리 변경 시 재검색
watch(() => route.query, () => {
  loadSearchQueryFromUrl();
  sendSearchRequest();
}, { immediate: true, deep: true });

// 필터링 (랜덤 배열 기준)
const filteredResults = computed(() => {
  const query = hotelNameSearchQuery.value.toLowerCase();

  return randomizedResults.value.filter(item => {
    // ✅ 이 부분을 추가하세요.
    // active 상태가 1이 아닌 호텔은 바로 필터링에서 제외합니다.
    if (item.active !== true) return false;

    if (query.length > 0) {
      const matchesName = item.hname.toLowerCase().includes(query);
      const matchesAddress = item.address.toLowerCase().includes(query);
      if (!(matchesName || matchesAddress)) return false;
    }
    if (selectedTypes.value.length && !selectedTypes.value.includes(item.type)) return false;
    if (item.minPrice < priceRange.value.min || item.minPrice > priceRange.value.max) return false;
    if (item.avgScore != null && item.avgScore < rating.value) return false;

    const itemServices = item.services?.map(s => s.serviceName) || [];
    if (selectedAmenities.value.length && !selectedAmenities.value.every(a => itemServices.includes(a))) {
      return false;
    }
    return true;
  });
});

// 정렬 적용
const sortedResults = computed(() => {
  const list = [...filteredResults.value];
  switch (sortOption.value) {
    case 'priceAsc':
      return list.sort((a, b) => a.minPrice - b.minPrice || b.avgScore - a.avgScore || a.id - b.id);
    case 'ratingDesc':
      return list.sort((a, b) => b.avgScore - a.avgScore || a.minPrice - b.minPrice || a.id - b.id);
    case 'random':
      return list; // ✅ 최초 섞인 순서 유지
    default:
      return list;
  }
});

// 평점 텍스트
const getRatingText = (score) => {
  if (score >= 4.5) return '최고에요';
  if (score >= 4.0) return '아주 좋아요';
  if (score >= 3.0) return '괜찮아요';
  return '보통이에요';
};

const addToWishlist = async (hId) => {
  try {
    const token = localStorage.getItem("accessToken");
    if (!token) throw new Error("JWT 토큰이 없습니다.");
    const response = await axios.post(
      `/api/wishlist/${hId}`,
      {},
      { headers: { Authorization: `Bearer ${token}` } }
    );
    wishlistItems.value.push({ hId, ...response.data });
  } catch (error) {
    console.error("찜 추가 실패", error);
    alert("찜 추가에 실패했습니다.");
  }
};

const removeFromWishlist = async (hId) => {
  if (!hId) return;
  try {
    const token = localStorage.getItem("accessToken");
    if (!token) throw new Error("JWT 토큰이 없습니다.");
    await axios.delete(`/api/wishlist/${hId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    wishlistItems.value = wishlistItems.value.filter(item => item.hId !== hId);
  } catch (error) {
    console.error("찜 해제 실패", error);
    alert("찜 해제에 실패했습니다.");
  }
};

const isFavorite = (hId) => wishlistItems.value.some(item => item.hId === hId);

const toggleFavorite = async (hId) => {
  if (!isLoggedIn.value) {
    router.push({ name: "Login" });
    return;
  }
  if (isFavorite(hId)) {
    await removeFromWishlist(hId);
  } else {
    await addToWishlist(hId);
  }
};

const translateType = (type) => typeMap[type] || type;

onMounted(async () => {
  if (!isLoggedIn.value) return;
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get('/api/wishlist', {
      headers: { Authorization: `Bearer ${token}` }
    });
    wishlistItems.value = response.data;
  } catch (error) {
    console.error("찜 목록 불러오기 실패", error);
  }
});
</script>
  
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap');
  
body {
  font-family: 'Noto Sans KR', sans-serif;
  color: #333;
  background-color: #FDFBF8;
}
  
.page-container { padding-bottom: 60px; }
.content-wrapper { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
  
.search-section { padding: 40px 0; background-color: #fff; border-bottom: 1px solid #e0e0e0; }
.main-search-bar { display: flex; align-items: center; background-color: #f5f6f7; border: 1px solid #E5E5E5; border-radius: 12px; height: 72px; box-shadow: 0 8px 16px rgba(0,0,0,0.05); max-width: 900px; margin: 0 auto;}
.search-input-group { display: flex; align-items: center; flex: 1 1 0; height: 100%; padding: 0 20px; cursor: pointer; gap: 10px; }
.search-input-group:not(:last-of-type) { border-right: 1px solid #E5E5E5; }
.search-input-group.destination { flex-grow: 1.5; }
.search-input-group input { border: none; font-size: 1rem; width: 100%; font-weight: 500; outline: none; color: #333; background: transparent; }
.search-input-group input::placeholder { color: #888; }
.date-text, .guests span { font-weight: 500; font-size: 1rem; color: #222; }
.nights-badge { background-color: #e0f4ff; color: #007bff; border-radius: 20px; padding: 4px 10px; margin-left: auto; font-size: 0.8rem; }
.search-button { background-color: #007bff; color: #fff; border: none; border-radius: 8px; height: 56px; display: flex; align-items: center; gap: 8px; font-weight: 700; padding: 0 24px; cursor: pointer; margin: 0 8px; transition: background-color 0.2s ease; }
.search-button:hover { background-color: #0056b3; }
  
.type-nav { background-color: #fff; border-bottom: 1px solid #eee; margin-bottom: 30px; }
.type-nav .content-wrapper { display: flex; gap: 5px; }
.type-nav button { flex: 1; padding: 18px 20px; border: none; border-bottom: 3px solid transparent; background-color: #fff; font-size: 1.1rem; font-weight: 600; color: #888; cursor: pointer; transition: all 0.2s ease-in-out; text-align: center; }
.type-nav button:hover { color: #333; }
.type-nav button.active { color: #007bff; border-bottom: 3px solid #007bff; }
  
.search-result-container { display: grid; grid-template-columns: 280px 1fr; gap: 40px; max-width: 1200px; margin: 40px auto; padding: 0 20px;   align-items: start;   /* ✅ 두 칼럼 위쪽 정렬 */}
.results-main-panel { min-width: 0; }
.search-summary { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; padding-bottom: 15px; }
.search-summary h2 { margin: 0; font-size: 1.8rem; font-weight: 700; color: #222; }
.search-summary h2 strong { color: #007bff; }
.sort-options select { padding: 10px 15px; border-radius: 8px; border: 1px solid #ddd; font-size: 1rem; color: #555; background-color: #fff; cursor: pointer; }
.results-list { display: flex; flex-direction: column; gap: 0; }
  
.filters-column { height: fit-content; top: 100px;   align-self: stretch;  /* ✅ 오른쪽 패널 높이에 맞게 늘어나도록 */}
.filter-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
.filter-header h4 { margin: 0; font-size: 1.3rem; font-weight: 700; color: #222; }
.btn-reset { background: none; border: none; color: #888; font-size: 0.9rem; cursor: pointer; padding: 0; }
.filter-group { border-bottom: 1px solid #f0f0f0; padding: 25px 0; }
.filters-column .filter-group:last-of-type { border-bottom: none; }
.filter-group h5 { margin: 0 0 15px 0; font-size: 1.1rem; font-weight: 600; color: #333; }
.type-button-group { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 10px; }
.type-filter-btn { padding: 8px 16px; border: 1px solid #ddd; border-radius: 20px; background-color: #fff; color: #555; font-size: 0.9rem; cursor: pointer; transition: all 0.2s; }
.type-filter-btn.active { background-color: #007bff; color: #fff; border-color: #007bff; }
.btn-more-types { background: none; border: none; color: #007bff; font-weight: 500; margin-top: 5px; cursor: pointer; }
.rating-filter-card {  display: flex;  flex-direction: column; /* 세로 정렬 */  gap: 8px;  background-color: #F8F9FA;  border-radius: 8px;  padding: 10px 15px;}
.rating-filter-wrapper {  display: flex;  align-items: center;  gap: 10px;}
.rating-filter {  display: flex;  gap: 4px;}
.rating-filter button {  background: none;  border: none;  font-size: 1.5rem;  color: #E0E0E0;  cursor: pointer;  padding: 0 4px;  transition: color 0.2s ease;}
.rating-filter button.active { color: #FFD700;}
.zero-btn { font-size: 0.85rem; color: #555;  padding: 2px 6px;  border: 1px solid #ddd;  border-radius: 12px;  background-color: #fff;  cursor: pointer;}
.zero-btn.active {  font-weight: 700;  color: #007bff;  border-color: #007bff;}
.rating-filter-card span { font-weight: 500;  color: #555;  flex: 1;  text-align: center; /* 중앙정렬 */}

.price-range-slider {
  position: relative;
  height: 20px;
  margin-bottom: 15px;
}
.price-range-slider .price-slider-min,
.price-range-slider .price-slider-max {
  -webkit-appearance: none;
  background: transparent;
  width: 100%;
  position: absolute;
  pointer-events: none;
  height: 20px;
  top: 0;
  margin: 0;
}
.price-range-slider .price-slider-min::-webkit-slider-thumb,
.price-range-slider .price-slider-max::-webkit-slider-thumb {
  -webkit-appearance: none;
  pointer-events: all;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #007bff;
  border: 3px solid #fff;
  box-shadow: 0 0 5px rgba(0,0,0,0.2);
  cursor: pointer;
  /* margin-top: -7px; <- 이 줄을 제거했습니다. */
}
.price-range-slider .price-slider-min::-moz-range-thumb,
.price-range-slider .price-slider-max::-moz-range-thumb {
  pointer-events: all;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #007bff;
  border: 3px solid #fff;
  box-shadow: 0 0 5px rgba(0,0,0,0.2);
  cursor: pointer;
}
/* 핵심: 기본 트랙을 투명하게 만듭니다 */
.price-range-slider .price-slider-min::-webkit-slider-runnable-track,
.price-range-slider .price-slider-max::-webkit-slider-runnable-track {
    background: transparent;
}
.price-range-slider .price-slider-min::-moz-range-track,
.price-range-slider .price-slider-max::-moz-range-track {
    background: transparent;
}

.price-range-slider .slider-track {
  position: absolute;
  width: 100%;
  height: 6px;
  top: 7px;
  border-radius: 3px;
  background: linear-gradient(
    to right,
    #E0E0E0 0%,
    #E0E0E0 var(--min-percent),
    #007bff var(--min-percent),
    #007bff var(--max-percent),
    #E0E0E0 var(--max-percent),
    #E0E0E0 100%
  );
}

.price-display { display: flex; justify-content: space-between; color: #333; font-weight: 600; margin-top: 10px; font-size: 1rem; }

.result-card { border-bottom: 1px solid #f0f0f0; padding: 20px 0; transition: background-color 0.2s; cursor: pointer; display: block; text-decoration: none; color: inherit; }
.results-list .result-card:last-child { border-bottom: none; }
.result-card:hover { background-color: #f9f9f9; }
.result-card-inner {
  display: grid;
  grid-template-columns: 300px 1fr 200px;
  gap: 25px;
  align-items: center; /* ✅ 아이템들을 세로 중앙에 정렬합니다. */
}
.image-wrapper { position: relative; width: 300px; height: 300px; overflow: hidden; border-radius: 12px; }
.image-wrapper img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s ease; }
.result-card:hover .image-wrapper img { transform: scale(1.05); }
.info-wrapper { flex-grow: 1; display: flex; flex-direction: column; gap: 8px; }
.info-header { display: flex; flex-direction: column; }
.info-badges { display: flex; align-items: center; gap: 8px; }
.item-type { font-size: 0.9rem; color: #888; }
h3 { margin: 5px 0; font-size: 1.4rem; font-weight: 700; color: #222; }
.rating-section { display: flex; align-items: center; gap: 10px; }
.rating-card { display: flex; align-items: center; gap: 5px; background-color: #FFFBEA; border-radius: 4px; padding: 5px 10px; }
.score-badge { background: none; color: #F7C44E; font-weight: 700; font-size: 1.1rem; }
.rating-text { font-size: 1rem; font-weight: 600; color: #F7C44E; }
.review-count { font-size: 0.9rem; color: #666; line-height: 1.5; }
.details-group { margin-top: 15px; padding-top: 15px; border-top: 1px solid #f0f0f0; display: flex; flex-direction: column; gap: 8px; font-size: 0.95rem; color: #555; }
.details-group p { margin: 0; line-height: 1.5; }
.hotel-grade-stars { color: #E91E63; }
.amenities strong { color: #333; }
.location-icon { margin-right: 5px; color: #777; }
  
.price-wrapper { width: 200px; display: flex; flex-direction: column; justify-content: space-between; align-items: flex-end; text-align: right; position: relative; }
.like-button { background: none; border: none; font-size: 1.8rem; cursor: pointer; color: #aaa; padding: 0; transition: all 0.2s ease; }
.like-button:hover { color: #E53935; transform: scale(1.1); }
.final-price-box { margin-top: auto; }
.final-price-box .price-label { font-size: 0.95rem; font-weight: 500; color: #555; }
.final-price-box strong { font-size: 1.7rem; font-weight: 800; color: #E53935; white-space: nowrap; min-width: 100%; }
  
.calendar-popup, .guest-selector-popup { position: absolute; background-color: #fff; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); border: 1px solid #E0E0E0; z-index: 1000; }
.guest-selector-popup { width: 320px; padding: 25px; }
.guest-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.counter { display: flex; align-items: center; gap: 15px; }
.counter button { width: 36px; height: 36px; border-radius: 50%; border: 1px solid #E0E0E0; background-color: #fff; font-size: 1.6rem; color: #888; cursor: pointer; }
.confirm-btn { background-color: #007bff; color: #fff; border: none; padding: 12px 25px; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 1rem; }
  
.pagination { display: flex; justify-content: center; gap: 10px; margin-top: 50px; }
.pagination a { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border: 1px solid #ddd; border-radius: 8px; text-decoration: none; color: #333; font-weight: 500; }
.pagination a.active { background-color: #007bff; color: #fff; border-color: #007bff; font-weight: 700; }

.checkbox-group {  display: flex;  flex-wrap: wrap;  gap: 10px 20px; /* 줄 간격 10px, 항목 간격 20px */}
.checkbox-group label {  width: calc(50% - 10px); /* 2줄 정렬: 전체 너비의 절반 */  display: flex;  align-items: center;}
.checkbox-label input {  margin-right: 8px; /* 체크박스와 텍스트 사이 간격 */}
.hotel-search-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box; /* 패딩이 너비에 포함되도록 설정 */
  margin-top: 5px; /* 필터 헤더와의 간격 조정 */
}
</style>