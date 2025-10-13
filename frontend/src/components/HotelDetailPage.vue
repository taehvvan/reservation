<template>
  <div v-if="hotel" class="hotel-detail-page">
    <section id="base" class="detail-header">
      <div class="image-gallery">
        <div class="main-image">
          <img :src="mainImage" :alt="hotel.hName">
        </div>
        <div class="sub-images">
          <img
            @error="onImageError"
            v-for="(image, index) in subImages"
            :key="index"
            :src="image"
            :alt="`${hotel.hName} 이미지 ${index + 2}`"
          >
          <div class="more-images-overlay">
            <button>+ 0</button>
          </div>
        </div>
      </div>
      <div class="header-content-wrapper">
        <div class="header-content">
          <div class="info-main">
            <p class="hotel-info-line">
              <span class="hotel-type-display">{{ hotel.type }}</span>
              <span class="elidia-level">
                <span v-for="i in hotel.star" :key="i" class="level-star">★</span>
              </span>
            </p>
            <h1>{{ hotel.hName }}</h1>
          </div>
          <div class="info-price">
            <button
              class="like-button"
              @click.prevent="toggleFavorite(hotel.hId)"
            >
              {{ isFavorite(hotel.hId) ? '❤️' : '♡' }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <div
      :class="['sticky-nav-bar', { 'is-sticky': isSticky }]"
      ref="stickyNavBarRef"
    >
      <div class="sticky-nav-content">
        <nav class="sticky-nav-links">
          <a href="#base" @click.prevent="scrollToSection('base')">개요</a>
          <a href="#rooms" @click.prevent="scrollToSection('rooms')">객실</a>
          <a href="#amenities" @click.prevent="scrollToSection('amenities')">시설</a>
          <a href="#map-section" @click.prevent="scrollToSection('map-section')">위치</a>
          <a href="#reviews-section" @click.prevent="scrollToSection('reviews-section')">리뷰</a>
        </nav>
        <div class="sticky-nav-booking">
          <span class="price-label" v-if="!noRoomsAvailable">최저가</span>
          <span class="price-highlight" v-if="!noRoomsAvailable">
            {{ hotel.minPrice.toLocaleString() }}원
          </span>
          <button
            class="sticky-nav-button"
            @click="scrollToSection('rooms')"
          >
            객실 보기
          </button>
        </div>
      </div>
    </div>

    <main class="detail-main-content">
      <div class="content-column">

        <div class="summary-cards-wrapper">
          <div class="summary-card rating-review-card">
            <h3 class="card-title">
              <div class="rating-badge-wrapper">
                <span class="score-badge-yellow">{{ hotel.avgScore.toFixed(1) }}</span>
                <span class="rating-text-yellow">{{ getRatingText(hotel.avgScore) }}</span>
              </div>
              <span class="review-count">({{ hotel.reviewCount.toLocaleString() }}명 평가)</span>
            </h3>
            <p class="review-quote-text" v-if="topRatedReview">
              "{{ topRatedReview.content }}"
            </p>
            <p v-else class="review-quote-text">
              "아직 리뷰가 없어요! 숙박하시고 리뷰를 작성해주세요!"
            </p>
          </div>
          <div class="summary-card amenities-preview-card">
            <h3 class="card-title">
              서비스 및 부대시설
              <span v-if="hotel.services.length > 6" class="more-link" @click.prevent="scrollToSection('amenities')">
                더보기
              </span>
            </h3>

            <div class="amenities-preview-list">
              <span
                v-for="(service, index) in hotel.services.slice(0, 6)"
                :key="index"
              >
                ✔️ {{ service.serviceName }}
              </span>
            </div>
          </div>
          <div class="summary-card location-preview-card">
            <h3 class="card-title">상세 주소</h3>
            <div class="location-preview-content">
              <span>📍 {{ hotel.address }}</span>
            </div>
          </div>
        </div>

        <section id="hotel-info" class="detail-section">
          <h2>숙소 이용 정보</h2>
          <ul>
            <li>{{ hotel.info }}</li>
            <br></br>
            <li>정확한 체크인/체크아웃 시간은 숙소에 문의해주세요.</li>
          </ul>
        </section>

        <section id="rooms" class="detail-section">
          <h2>객실 선택</h2>

          <div class="search-conditions-form">
            <label>체크인: <input type="date" v-model="checkInString" /></label>
            <label>체크아웃: <input type="date" v-model="checkOutString" /></label>
            <label>객실 수: <input type="number" min="1" v-model.number="rooms" /></label>
            <label>인원: <input type="number" min="1" v-model.number="persons" /></label>
            <button @click="updateSearchConditions">검색 적용</button>
          </div>

          <div v-if="noRoomsAvailable" class="rooms-content sold-out-mode">
            <div class="sold-out-message">
              선택한 날짜의 객실은 매진되었어요.<br/>
              상단 검색창에서 날짜나 인원을 다시 설정해 보세요.
            </div>
          </div>

          <div v-else class="rooms-content">
            <div class="room-list">
              <div v-for="room in hotel.rooms" :key="room.rId" class="room-card">
                <div class="room-image">
                  <img :src="getRoomImageUrl(room)" :alt="room.type" @error="onImageError">
                </div>
                <div class="room-info">
                  <h4>{{ room.type }}</h4>
                  <p class="room-spec">
                    <span>🛏️ 최대 {{ room.people }}명</span>
                    <span>✅ 재고: {{ minAvailableCounts[room.rId] }}개</span>
                    <span>⏰ 체크인: {{ room.checkinTime }} ~ 체크아웃: {{ room.checkoutTime }}</span>
                  </p>
                </div>
                <div class="room-booking">
                  <div class="room-price-block">
                    <span class="price-highlight">{{ room.price.toLocaleString() }}원</span>
                    <span class="price-subtext">세금 및 봉사료 포함</span>
                  </div>
                  <button
                    class="btn-book"
                    @click="goToCheckout(room, minAvailableCounts[room.rId])"
                    :disabled="
                      minAvailableCounts[room.rId] <= 0 ||
                      persons > room.people * (room.selectedQuantity || 1)
                    ">
                    {{
                      minAvailableCounts[room.rId] <= 0
                        ? '예약 마감'
                        : persons > room.people * (room.selectedQuantity || 1)
                          ? '인원 초과'
                          : '예약하기'
                    }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>


           <!-- Amenities -->
           <section id="amenities" class="detail-section">
          <h2>서비스 및 부대시설</h2>
          <div class="amenities-grid">
            <span v-for="(service, index) in hotel.services" :key="index">✔️ {{ service.serviceName }}</span>
          </div>
        </section>

        <!-- Map -->
        <section id="map-section" class="detail-section map-section">
          <h2>위치</h2>
          <div class="map-placeholder">
            <div id="kakao" :style="{width: '100%', height: '400px'}"></div>
          </div>
        </section>

        <!-- Landmarks -->
        <section id="nearby-attractions" class="detail-section nearby-attractions">
          <h2>근처 랜드마크</h2>
          <div class="attraction-list">
            <div v-for="place in nearbyLandmarks" :key="place.name" class="attraction-item">
          
              <div class="attraction-info">
                <strong>{{ place.name }}</strong>
                <p>차량 {{ place.minutes }}분 ({{ place.distanceKm }} km)</p>
              </div>
            </div>
          </div>
        </section>

        <!-- Reviews -->
        <section id="reviews-section" class="detail-section reviews-section">
          <div class="reviews-header">
            <h2>리얼 리뷰</h2>
            <div class="rating-badge-wrapper">
              <span class="score-badge-yellow">{{ hotel.avgScore.toFixed(1) }}</span>
              <span class="review-count">({{ hotel.reviewCount.toLocaleString() }}개 평가)</span>
            </div>
          </div>

          <div class="review-list" v-if="hotel.reviews && hotel.reviews.length > 0">
            <div v-for="review in hotel.reviews" :key="review.reviewId" class="review-card">
              <div class="review-header">
                <img src="https://placehold.co/40x40?text=U" alt="프로필" class="user-profile-img">
                <div class="user-info">
                  <strong class="user-nickname">사용자 {{ review.userName }}</strong>
                  <span class="review-date">{{ formatDateTime(review.createdAt) }}</span>
                  
                </div>
                <div class="review-rating">
                  <span
                    v-for="i in 5"
                    :key="i"
                    :class="{ filled: i <= review.score }"
                  >
                    ★
                  </span>
                </div>
              </div>

              <p class="review-text">{{ review.content }}</p>

              <div v-if="review.image" class="review-images">
                <img :src="review.image" alt="리뷰 이미지">
              </div>

              <!-- ✅ 답글 영역 -->
              <div v-if="review.reply" class="review-reply">
                <div class="reply-header">
                  <strong class="reply-author">🏨 숙소 관리자</strong>
                  <span class="reply-date">{{ formatDateTime(review.repliedAt) }}</span>
                </div>
                <p class="reply-text">{{ review.reply }}</p>
              </div>
            </div>
          </div>

          <!-- 리뷰가 없을 때 -->
          <div v-else class="reviews-empty-box">
            아직 리뷰가 없어요. 숙박 후 첫 번째 후기를 남겨주세요!
          </div>
        </section>

        </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed, toRaw } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useBookingStore } from '@/stores/booking';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const bookingStore = useBookingStore();
const authStore = useAuthStore();

const stickyNavBarRef = ref(null);
const hId = ref(route.params.id);
const checkIn = ref(null);
const checkOut = ref(null);
const rooms = ref(1);
const persons = ref(2);

const isLoggedIn = ref(!!localStorage.getItem("accessToken"));
const wishlistItems = ref([]);
const hotel = ref(null);
const isSticky = ref(false);
const CAR_SPEED = 20;

const landmarks = ref([
  // 서울
  { name: "경복궁", lat: 37.579617, lon: 126.977041 },
  { name: "N서울타워", lat: 37.551169, lon: 126.988227 },
  { name: "북촌 한옥마을", lat: 37.5828, lon: 126.9834 },
  { name: "창덕궁", lat: 37.5794, lon: 126.9911 },
  { name: "롯데월드타워 & 롯데월드몰", lat: 37.5126, lon: 127.1025 },
  { name: "동대문디자인플라자 (DDP)", lat: 37.5665, lon: 127.0095 },
  { name: "코엑스 스타필드", lat: 37.5121, lon: 127.0589 },
  { name: "홍대거리", lat: 37.5569, lon: 126.9239 },
  { name: "명동거리", lat: 37.5634, lon: 126.9841 },
  { name: "광장시장", lat: 37.5701, lon: 126.9996 },

  // 부산
  { name: "해운대해수욕장", lat: 35.1587, lon: 129.1604 },
  { name: "광안리해수욕장 & 광안대교", lat: 35.1531, lon: 129.1186 },
  { name: "감천문화마을", lat: 35.0979, lon: 129.0106 },
  { name: "해동용궁사", lat: 35.1892, lon: 129.2235 },
  { name: "자갈치시장", lat: 35.0963, lon: 129.0304 },
  { name: "태종대", lat: 35.0522, lon: 129.0881 },
  { name: "송도해상케이블카", lat: 35.0784, lon: 129.0193 },
  { name: "더베이 101", lat: 35.1578, lon: 129.1517 },
  { name: "국제시장", lat: 35.1011, lon: 129.0263 },
  { name: "흰여울문화마을", lat: 35.0792, lon: 129.0436 },

  // 강릉
  { name: "경포해변", lat: 37.7954, lon: 128.9146 },
  { name: "안목해변 커피거리", lat: 37.7711, lon: 128.9472 },
  { name: "오죽헌", lat: 37.7788, lon: 128.8784 },
  { name: "정동진해변", lat: 37.691, lon: 129.0358 },
  { name: "강릉중앙시장", lat: 37.7543, lon: 128.8981 },
  { name: "하슬라아트월드", lat: 37.7126, lon: 129.0151 },
  { name: "주문진항", lat: 37.8943, lon: 128.8318 },
  { name: "아르떼뮤지엄 강릉", lat: 37.785, lon: 128.9056 },
  { name: "강문해변", lat: 37.7905, lon: 128.9216 },
  { name: "도깨비 촬영지 (주문진)", lat: 37.915, lon: 128.8239 },

  // 속초
  { name: "설악산 국립공원", lat: 38.1188, lon: 128.4912 },
  { name: "속초해수욕장", lat: 38.1923, lon: 128.6044 },
  { name: "아바이마을", lat: 38.2078, lon: 128.5951 },
  { name: "속초관광수산시장", lat: 38.2052, lon: 128.5901 },
  { name: "영금정", lat: 38.211, lon: 128.6013 },
  { name: "대포항", lat: 38.1726, lon: 128.6146 },
  { name: "울산바위", lat: 38.1772, lon: 128.4711 },
  { name: "척산족욕공원", lat: 38.1883, lon: 128.5445 },
  { name: "속초아이 대관람차", lat: 38.1921, lon: 128.6041 },
  { name: "외옹치항", lat: 38.1812, lon: 128.6111 },

  // 인천
  { name: "차이나타운", lat: 37.4746, lon: 126.6184 },
  { name: "월미도", lat: 37.4716, lon: 126.5968 },
  { name: "송도센트럴파크", lat: 37.3949, lon: 126.6347 },
  { name: "을왕리해수욕장", lat: 37.4475, lon: 126.3719 },
  { name: "강화도", lat: 37.747, lon: 126.491 },
  { name: "소래포구", lat: 37.3871, lon: 126.7388 },
  { name: "인천대교", lat: 37.45, lon: 126.55 },
  { name: "전등사", lat: 37.6438, lon: 126.4385 },
  { name: "신포국제시장", lat: 37.4704, lon: 126.6258 },
  { name: "송월동 동화마을", lat: 37.4764, lon: 126.6186 },

  // 제주
  { name: "성산일출봉", lat: 33.458, lon: 126.9423 },
  { name: "한라산 국립공원", lat: 33.3617, lon: 126.5292 },
  { name: "협재해수욕장", lat: 33.3944, lon: 126.2396 },
  { name: "우도", lat: 33.5029, lon: 126.9611 },
  { name: "오설록 티 뮤지엄", lat: 33.3059, lon: 126.2891 },
  { name: "주상절리대", lat: 33.2407, lon: 126.4259 },
  { name: "카멜리아힐", lat: 33.2842, lon: 126.3756 },
  { name: "함덕해수욕장", lat: 33.543, lon: 126.669 },
  { name: "천지연폭포", lat: 33.2443, lon: 126.5599 },
  { name: "만장굴", lat: 33.5292, lon: 126.7719 }
]);

// --- ✅ [추가] 이미지 경로를 동적으로 생성하는 computed 속성 ---

// 호텔의 메인 이미지를 반환합니다.
const mainImage = computed(() => {
  if (!hotel.value) return '';
  // 백엔드에서 hotel.images 배열을 보내준다면, type이 'main'인 것을 찾습니다.
  const main = hotel.value.images?.find(img => img.imageType === 'main');
  if (main) return main.imageUrl;
  // 그렇지 않으면 기존 방식대로 생성합니다.
  return `${import.meta.env.VITE_APP_API_URL}/images/${hotel.value.type}/${hotel.value.hId}.jpg`;
});

// 호텔의 서브 이미지들을 반환합니다.
const subImages = computed(() => {
  if (!hotel.value) return [];
  // 백엔드에서 hotel.images 배열을 보내준다면, type이 'sub'인 것들을 찾습니다.
  const subs = hotel.value.images?.filter(img => img.imageType === 'sub');
  if (subs && subs.length > 0) return subs.map(img => img.imageUrl);
  // 그렇지 않으면 기존 방식대로 3개의 이미지를 생성합니다.
  return Array.from({ length: 3 }, (_, i) => `${import.meta.env.VITE_APP_API_URL}/images/${hotel.value.type}/${hotel.value.hId}_${i + 1}.jpg`);
});

// 객실 이미지 URL을 생성하는 함수입니다.
const getRoomImageUrl = (room) => {
  if (!hotel.value || !room) return 'https://placehold.co/400x200.png?text=No+Image';
  // 객실 이미지 파일명 규칙에 따라 URL을 생성합니다.
  return `${import.meta.env.VITE_APP_API_URL}/images/${hotel.value.type}/rooms/${room.rId}.jpg`;
};

// --- (이하 나머지 스크립트는 기존과 거의 동일) ---

const minAvailableCounts = computed(() => {
  const counts = {};
  if (hotel.value && hotel.value.rooms) {
    hotel.value.rooms.forEach(room => {
      if (!room.availabilities || room.availabilities.length === 0) {
        counts[room.rId] = 0;
      } else {
        counts[room.rId] = Math.min(...room.availabilities.map(a => a.availableCount));
      }
    });
  }
  return counts;
});

const handleScroll = () => {
  const header = document.querySelector('.detail-header');
  if (!header) return;
  const headerBottom = header.offsetTop + header.offsetHeight;
  isSticky.value = window.scrollY >= headerBottom;
};

const getRatingText = (rating) => {
  if (rating >= 4.5) return '최고에요';
  if (rating >= 4.0) return '아주 좋아요';
  if (rating >= 3.0) return '괜찮아요';
  return '보통이에요';
};

function getDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * Math.PI / 180) *
    Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

const nearbyLandmarks = computed(() => {
  if (!hotel.value) return [];

  return landmarks.value
    .map(l => {
      const distanceKm = getDistanceKm(hotel.value.latitude, hotel.value.longitude, l.lat, l.lon);
      const minutes = Math.round((distanceKm / CAR_SPEED) * 60);
      return { ...l, distanceKm: distanceKm.toFixed(1), minutes };
    })
    .sort((a, b) => a.distanceKm - b.distanceKm)
    .slice(0, 6);
});
const loadDetailQueryFromUrl = () => {
  const query = route.query;
  checkIn.value = query.startDate ? new Date(query.startDate) : null;
  checkOut.value = query.endDate ? new Date(query.endDate) : null;
  rooms.value = Number(query.rooms) || 1;
  persons.value = Number(query.persons) || 2;
  hId.value = route.params.id || null;
};

const sendDetailSearchRequest = async () => {
  if (!hId.value || isNaN(Number(hId.value))) {
    console.error("Hotel ID is not valid. Cancelling API request.");
    return;
  }

  const requestBody = {
    hId: Number(hId.value),
    startDate: checkIn.value ? checkIn.value.toISOString().split('T')[0] : null,
    endDate: checkOut.value ? checkOut.value.toISOString().split('T')[0] : null,
    numberOfRooms: Number(rooms.value),
    numberOfPeople: Number(persons.value),
  };

  try {
    const response = await fetch('/api/detail', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody),
    });

    if (response.ok) {
      hotel.value = await response.json();
      console.log('데이터 로드 성공:', hotel.value);
    } else {
      console.error('디테일 검색 실패:', response.status);
    }
  } catch (error) {
    console.error('API 호출 중 예외 발생:', error);
  }
};

const goToCheckout = (room, availableCount) => {
  if (!hotel.value) {
    alert("호텔 정보가 로드되지 않았습니다.");
    return;
  }
  if (availableCount <= 0) {
    alert('해당 객실은 현재 예약이 불가능합니다.');
    return;
  }
  if (!checkIn.value || !checkOut.value) {
    alert('체크인 및 체크아웃 날짜를 선택해주세요.');
    return;
  }

  const maxCapacity = room.people * (room.selectedQuantity || 1);
  if (persons.value > maxCapacity) {
    alert(`선택한 객실로는 ${persons.value}명을 수용할 수 없습니다. (최대 ${maxCapacity}명 가능)`);
    return;
  }

  bookingStore.setBookingDetails({
    hotel: toRaw(hotel.value),
    room: toRaw(room),
    checkIn: checkIn.value.toISOString().split('T')[0],
    checkout: checkOut.value.toISOString().split('T')[0],
    guests: persons.value,
  });

  console.log("📦 bookingStore에 예약 정보 저장 완료:", bookingStore.$state);
  router.push('/checkout');
};

const onImageError = (event) => {
  event.target.src = 'https://placehold.co/400x400.png?text=No+Image';
};

const scrollToSection = (id) => {
  const element = document.getElementById(id);
  if (element) {
    const offset = stickyNavBarRef.value ? stickyNavBarRef.value.offsetHeight : 0;
    window.scrollTo({
      top: element.offsetTop - offset - 20,
      behavior: 'smooth',
    });
  }
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});

const loadKakaoMap = () => {
  return new Promise((resolve, reject) => {
    if (window.kakao && window.kakao.maps) {
      resolve(window.kakao);
      return;
    }
    const script = document.createElement('script');
    script.src = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=e77831e9ccd11f157f3055f8800d5602&autoload=false";
    script.onload = () => {
      if (window.kakao && window.kakao.maps) {
        resolve(window.kakao);
      } else {
        reject(new Error("Kakao Maps SDK 로드 실패"));
      }
    };
    script.onerror = () => reject(new Error("Kakao Maps SDK 스크립트 로드 실패"));
    document.head.appendChild(script);
  });
};

const initMap = async (hname) => {
  try {
    const kakao = await loadKakaoMap();
    if (!kakao || !kakao.maps) return;
    kakao.maps.load(() => {
      const container = document.getElementById('kakao');
      if (!container) return;
      const lat = hotel.value.latitude;
      const lng = hotel.value.longitude;
      const map = new kakao.maps.Map(container, {
        center: new kakao.maps.LatLng(lat, lng),
        level: 5
      });
      const hotelMarker = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(lat, lng)
      });
      hotelMarker.setMap(map);
      const hotelInfo = new kakao.maps.InfoWindow({
        content: `
          <div title="${hname}" style="
            padding:6px;
            text-align:center;
            font-weight:600;
            max-width:180px;
            white-space:nowrap;
            overflow:hidden;
            text-overflow:ellipsis;
          ">
            ${hname}
          </div>`
      });
      hotelInfo.open(map, hotelMarker);
      const landmarkMarkerImage = new kakao.maps.MarkerImage(
        "https://cdn-icons-png.flaticon.com/512/854/854878.png",
        new kakao.maps.Size(32, 32),
        { offset: new kakao.maps.Point(16, 32) }
      );
      // 1) 동그란 검은색 원 마커 이미지 만들기
      const circleMarkerImage = new kakao.maps.MarkerImage(
        "data:image/svg+xml;base64," + btoa(`
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">
            <circle cx="8" cy="8" r="6" fill="black" />
          </svg>
        `),
        new kakao.maps.Size(16, 16),
        { offset: new kakao.maps.Point(8, 8) } // 중심 맞추기
      );

      // 2) 랜드마크에 동그란 마커 적용
      nearbyLandmarks.value.forEach(place => {
        const pos = new kakao.maps.LatLng(place.lat, place.lon);
        const marker = new kakao.maps.Marker({
          position: pos,
          image: circleMarkerImage  // ✅ 동그란 마커 적용
        });
        marker.setMap(map);

        const iwContent = `
          <div style="padding:6px;min-width:140px;white-space:nowrap;">
            <strong>${place.name}</strong><br/>
            ${place.distanceKm} km, 차량 ${place.minutes}분
          </div>`;
        const infowindow = new kakao.maps.InfoWindow({ content: iwContent });

        kakao.maps.event.addListener(marker, 'click', () => {
          infowindow.open(map, marker);
        });
      });
    });
  } catch (e) {
    console.error("Kakao Map 로드 실패:", e);
  }
};

const noRoomsAvailable = computed(() => {
  if (!hotel.value || !hotel.value.rooms) return true;
  return Object.values(minAvailableCounts.value).every(count => count === 0);
});

const topRatedReview = computed(() => {
  if (!hotel.value || !hotel.value.reviews || hotel.value.reviews.length === 0) return null;
  return hotel.value.reviews.reduce((max, review) => {
    return review.score > (max?.score || 0) ? review : max;
  }, null);
});

watch(hotel, async (newVal) => {
  if (newVal) {
    await nextTick();
    initMap(newVal.hName);
  }
});

watch(hotel, (newVal) => {
  if (newVal && newVal.rooms) {
    const defaultQuantity = rooms.value || 1;
    newVal.rooms.forEach(room => {
      room.selectedQuantity = defaultQuantity;
    });
  }
});

watch(
  () => route.query,
  () => {
    loadDetailQueryFromUrl();
    sendDetailSearchRequest();
  },
  { immediate: true, deep: true }
);

const addToWishlist = async (hId) => {
  try {
    const token = localStorage.getItem("accessToken");
    if (!token) throw new Error("JWT 토큰이 없습니다.");
    const response = await axios.post(`/api/wishlist/${hId}`, {}, { headers: { Authorization: `Bearer ${token}` } });
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
    await axios.delete(`/api/wishlist/${hId}`, { headers: { Authorization: `Bearer ${token}` } });
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

const formatDateTime = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

onMounted(async () => {
  if (!isLoggedIn.value) return;
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get('/api/wishlist', { headers: { Authorization: `Bearer ${token}` } });
    wishlistItems.value = response.data.map(item => ({
      hId: item.hId,
      ...item
    }));
  } catch (error) {
    console.error("찜 목록 불러오기 실패", error);
  }
});

const checkInString = computed({
  get: () => checkIn.value ? checkIn.value.toISOString().split("T")[0] : "",
  set: (val) => { checkIn.value = val ? new Date(val) : null; }
});

const checkOutString = computed({
  get: () => checkOut.value ? checkOut.value.toISOString().split("T")[0] : "",
  set: (val) => { checkOut.value = val ? new Date(val) : null; }
});

const updateSearchConditions = () => {
  router.replace({
    query: {
      startDate: checkInString.value,
      endDate: checkOutString.value,
      rooms: rooms.value,
      persons: persons.value,
    },
  });
};
</script>

<style>
  /* --- 1. 상단 헤더 및 이미지 갤러리 --- */
  .detail-header {
    width: 100%;
    padding-top: 20px;
    background-color: #fff;
  }
  .image-gallery { max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: 2fr 1fr; grid-template-rows: 250px 250px; gap: 10px; height: 510px; }
  .main-image { grid-column: 1 / 2; grid-row: 1 / 3; }
  .main-image img, .sub-images img { width: 100%; height: 100%; object-fit: cover; border-radius: 4px; }
  .main-image { border-radius: 8px; overflow: hidden; }
  .sub-images { grid-column: 2 / 3; grid-row: 1 / 3; display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 10px; position: relative; }
  .more-images-overlay { position: absolute; bottom: 10px; right: 10px; }
  .more-images-overlay button { background-color: rgba(0,0,0,0.7); color: white; border: none; border-radius: 8px; padding: 10px 15px; font-weight: 500; cursor: pointer; }
  .header-content-wrapper { border-bottom: 10px solid #F9F9F9; }
  .header-content { max-width: 1200px; margin: 0 auto; padding: 25px 20px; display: flex; justify-content: space-between; align-items: center; }
  
  .info-main {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  .hotel-info-line {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    font-size: 1rem;
    color: #555;
    font-weight: 500;
  }
  .hotel-type-display { margin-right: 10px; }
  /* [수정] 엘리디아 레벨 별 디자인 */
  .elidia-level { display: flex; align-items: center; gap: 4px; color: #E53935; } /* 다홍색 별 */
  .level-star { font-size: 1.2rem; }
  
  h1 { font-size: 2.2rem; font-weight: 800; margin: 0; line-height: 1.2; color: #222; }
  .hotel-english-name { font-size: 1.1rem; color: #666; margin-top: 5px; font-weight: 400; }
  
  .info-price { display: flex; align-items: center; gap: 15px; }
  .info-price .like-button { background: none; border: 1px solid #ddd; border-radius: 50%; width: 48px; height: 48px; font-size: 1.8rem; cursor: pointer; color: #555; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
  .info-price .like-button:hover { background-color: #f0f0f0; color: #E53935; }
  
  /* --- 2. 스티키 네비게이션 바 --- */
  .sticky-nav-bar {
    width: 100%;
    background-color: #fff;
    border-bottom: 1px solid #eee;
    transition: all 0.3s ease;
    /* 초기엔 normal flow */
    position: relative; 
  }

  .sticky-nav-bar.is-sticky {
    position: fixed;
    top: 0;
    left: 0;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    z-index: 1000;
  }

  .sticky-nav-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .sticky-nav-links {
    display: flex;
    gap: 25px;
  }
  .sticky-nav-links a {
    text-decoration: none;
    color: #555;
    font-weight: 600;
    font-size: 1rem;
    padding: 5px 0;
    position: relative;
    transition: color 0.2s;
  }
  .sticky-nav-links a:hover {
    color: #007bff;
  }
  
  .sticky-nav-booking {
    display: flex;
    align-items: center;
    gap: 15px;
  }
/* 상단 스티키바 가격 */
.sticky-nav-price {
  font-size: 1.8rem;     /* 기존 1.5rem → 크게 */
  font-weight: 900;      /* 두껍게 */
  color: #E53935;        /* 진한 빨간색 */
}
  .sticky-nav-button {
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 8px;
    padding: 10px 25px;
    font-size: 1rem;
    font-weight: 700;
    cursor: pointer;
    white-space: nowrap;
  }
  
  
  /* --- 3. 메인 콘텐츠 --- */
  .detail-main-content { max-width: 1200px; margin: 0 auto; padding: 20px; display: block;
     /*grid-template-columns: 1fr 320px; gap: 30px;*/ }
  .content-column { min-width: 0; }
  .sidebar-column { min-width: 0; }
  
  .detail-section { 
    background-color: #fff;
    padding: 30px;
    margin-top: 20px;
    border-radius: 12px;
  }
  .detail-section h2 { 
    font-size: 1.8rem; 
    font-weight: 700; 
    padding-bottom: 15px; 
    margin: 0 0 25px 0; 
    border-bottom: 1px solid #eee; 
    display: flex;
    align-items: center;
  }
  .detail-section h2 .rating-badge-wrapper {
    margin-left: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  
  /* [수정] summary-cards-wrapper와 개별 카드 스타일 */
  .summary-cards-wrapper {
    display: grid;
    grid-template-columns: 1.5fr 1fr 1fr; /* 후기/평점 카드를 더 넓게 */
    gap: 10px; /* 카드 간 간격 */
    margin-top: 20px;
    margin-bottom: 30px;
  }
  .summary-card {
    background-color: #fff;
    padding: 20px; /* 패딩 조정 */
    border-radius: 12px;
    border: 1px solid #eee;
    box-shadow: 0 2px 5px rgba(0,0,0,0.03);
    display: flex;
    flex-direction: column;
  }
  
  /* 카드 제목 스타일 */
  .card-title {
    font-size: 1.1rem;
    font-weight: 700;
    color: #222;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .card-title .arrow-icon {
    font-size: 1.2rem;
    color: #888;
  }
  
  /* [수정] 평점, 후기 카드 - 이미지에 맞게 재구성 */
  .rating-review-card {
    display: flex;
    flex-direction: column;
    justify-content: center; /* 세로 가운데 */
    align-items: center;     /* 가로 가운데 */
    text-align: center;      /* 텍스트도 가운데 정렬 */
  }
  .rating-review-card .card-title {
    justify-content: center; /* space-between 대신 중앙 정렬 */
    gap: 10px; /* 아이콘과 글자 간격 */
  }
  .rating-badge-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .score-badge-yellow {
    background-color: #F7C44E; /* 노란색 배경 */
    color: #fff; /* 흰색 글씨 */
    padding: 6px 10px;
    border-radius: 6px;
    font-weight: 700;
    font-size: 1.1rem;
  }
  .rating-text-yellow {
    font-size: 1.1rem;
    font-weight: 600;
    color: #E8A800; /* 노란색 계열 글씨 */
  }
  .rating-review-card .review-count {
    font-size: 0.95rem;
    color: #666;
    font-weight: 500;
    margin-left: 8px;
  }
  .review-quote-text { 
    padding-left: 15px; 
    font-size: 0.95rem; 
    color: #555; 
    line-height: 1.5; 
    margin-top: auto; /* 하단 정렬 */
    padding-top: 15px;
    flex-grow: 1; /* 남은 공간 차지 */
  }
  
  /* 서비스 및 위치 정보 카드 */
  .amenities-preview-list, .location-preview-content { 
    font-size: 0.95rem; 
    color: #555; 
    display: flex; 
    flex-direction: column; 
    gap: 10px; 
    flex-grow: 1; /* 남은 공간 차지 */
    justify-content: center; /* 세로 중앙 정렬 */
  }
  .location-preview-content { flex-direction: row; justify-content: space-between; align-items: center; }
  .btn-map { font-size: 0.9rem; color: #007bff; text-decoration: none; font-weight: 500; }
  
  /* 객실 선택 */
  .room-list { display: flex; flex-direction: column; gap: 15px; }
  .room-card { display: grid; grid-template-columns: 200px 1fr auto; gap: 20px; padding: 20px; border: 1px solid #eee; border-radius: 12px; transition: box-shadow 0.2s; }
  .room-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
  .room-image img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
  .room-info h4 { margin: 0 0 10px 0; font-size: 1.2rem; font-weight: 600; }
  .room-info .room-spec { display: flex; flex-direction: column; gap: 8px; font-size: 0.9rem; color: #333; }
  .btn-more-info { font-size: 0.9rem; color: #555; text-decoration: none; margin-top: 10px; display: inline-block; }
.room-booking {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: flex-start;
  gap: 10px; /* 요소 간 간격 */
}

.room-price-block {
  display: flex;
  flex-direction: column;
  align-items: flex-end; /* 가격과 안내 문구 오른쪽 정렬 */
}
  .btn-book { background-color: #007bff; color: white; border: none; border-radius: 8px; padding: 12px 30px; font-size: 1rem; font-weight: 700; cursor: pointer; }
  
  /* 서비스 및 부대시설, 숙소 이용 정보 */
  .amenities-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; font-size: 0.95rem; }
  #hotel-info ul { list-style: none; padding: 0; }
  #hotel-info li { margin-bottom: 10px; font-size: 0.95rem; color: #555; }
  
  /* [추가] 지도 섹션 */
  .map-section .map-placeholder {
    width: 100%;
    height: 400px; /* 지도 높이 */
    background-color: #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    overflow: hidden;
  }
  .map-section .map-placeholder img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  /* [추가] 근처 문화재, 랜드마크 섹션 */
  .nearby-attractions .attraction-list {
    display: flex;
    gap: 20px;
    overflow-x: auto; /* 가로 스크롤 가능 */
    padding-bottom: 10px; /* 스크롤바 공간 */
  }
  .nearby-attractions .attraction-item {
    flex-shrink: 0; /* 아이템이 줄어들지 않도록 */
    width: 180px; /* 아이템 너비 */
    background-color: #f9f9f9;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 5px rgba(0,0,0,0.05);
    text-align: center;
    padding-bottom: 15px;
  }
  .nearby-attractions .attraction-item img {
    width: 100%;
    height: 120px;
    object-fit: cover;
    margin-bottom: 10px;
  }
  .nearby-attractions .attraction-info strong {
    display: block;
    font-size: 1rem;
    color: #222;
    margin-bottom: 5px;
  }
  .nearby-attractions .attraction-info p {
    font-size: 0.9rem;
    color: #777;
    margin: 0;
  }
  
  /* [추가] 리뷰 칸 섹션 (이미지 기반 디자인) */
  .reviews-section h2 {
  border-bottom: none;  /* ✅ 제목에는 선 제거 */
  margin-bottom: 0;     /* 불필요한 여백도 줄이기 */
  padding-bottom: 0;
}
  .reviews-section h2 .rating-badge-wrapper {
    margin-left: 0; /* 전체 제목의 일부로 간주 */
  }
  .reviews-section .sort-reviews {
    font-size: 0.95rem;
    color: #555;
    text-decoration: none;
    font-weight: 500;
  }
  .review-list {
    display: flex;
    flex-direction: column;
    gap: 30px;
  }
  .review-card {
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 25px;
  }
  .review-list .review-card:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
  .reviews-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
    margin-bottom: 25px;
  }

  /* 여백을 조금 벌리기 위해 */
  .reviews-header h2 {
    margin-right: 20px; /* 👉 제목과 오른쪽 요소 사이 공간 */
  }

  .rating-badge-wrapper.spaced {
    margin-left: auto; /* 👉 오른쪽으로 붙이되, h2와 간격 확보 */
  }
  .user-profile-img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
  }
  .review-header {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .user-info {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 5px 10px;
  }
  .user-nickname {
    font-weight: 700;
    font-size: 1.05rem;
    color: #222;
  }
  .user-level, .user-visits, .review-date {
    font-size: 0.85rem;
    color: #777;
  }
  .review-rating {
    margin-left: auto;  /* 👉 리뷰 헤더 오른쪽 끝으로 밀어냄 */
    display: flex;
    gap: 2px;           /* 별 간격 */
  }
  .review-rating span {
    font-size: 1.1rem;
    color: #ddd; /* 기본은 회색(빈 별) */
  }

  .review-rating span.filled {
    color: #FFD700; /* 채워진 별은 노란색 */
  }
  .review-images {
    display: flex;
    gap: 8px;
    margin: 15px 0;
    overflow-x: auto;
    padding-bottom: 5px; /* 스크롤바 공간 */
    position: relative;
  }
  .review-images img {
    width: 150px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
  }
  .more-review-images-overlay {
    position: absolute;
    right: 0;
    bottom: 5px; /* 스크롤바 위 */
    background-color: rgba(0,0,0,0.6);
    color: #fff;
    padding: 5px 10px;
    border-radius: 0 0 8px 8px;
    font-size: 0.9rem;
    font-weight: 500;
    pointer-events: none; /* 클릭 방지 */
  }
  .review-room-info {
    font-size: 0.9rem;
    color: #555;
    margin-bottom: 10px;
  }
  .review-text {
    font-size: 0.95rem;
    color: #333;
    line-height: 1.6;
    margin-top: 12px;   /* 👈 위쪽 공간 벌려주기 */
    margin-bottom: 10px;
  }
  .btn-more-review {
    background: none;
    border: none;
    color: #007bff;
    font-weight: 500;
    cursor: pointer;
    padding: 0;
    margin-bottom: 15px;
  }
  .review-actions {
    display: flex;
    gap: 10px;
  }
  .like-review-button, .report-review-button {
    background-color: #f0f0f0;
    border: none;
    border-radius: 20px;
    padding: 8px 15px;
    font-size: 0.9rem;
    color: #555;
    cursor: pointer;
  }
  .room-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0;
  font-size: 0.9rem;
  color: #333;
}
.room-quantity select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.price-highlight {
  font-size: 1.8rem;
  font-weight: 900;
  color: #E53935; /* 빨간색 */
  line-height: 1.2;
}
.price-label {
  font-size: 1rem;
  font-weight: 600;
  color: #555;
  margin-right: 5px;
}
.price-subtext {
  font-size: 0.85rem;
  color: #777;
  margin-top: 4px;
  display: block;
}

.search-conditions-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 20px;
}
.search-conditions-form label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
  color: #555;
}
.search-conditions-form input {
  margin-top: 5px;
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}
.search-conditions-form button {
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 600;
  cursor: pointer;
}
.rooms-content {
  margin-top: 15px;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff; /* 기본 흰색 */
}

/* 매진일 때 회색 처리 */
.rooms-content.sold-out-mode {
  background-color: #f5f5f5;
  color: #666;
  text-align: center;
}

.sold-out-message {
  padding: 50px 20px;
  font-size: 1.1rem;
  font-weight: 500;
  line-height: 1.6;
}

.coupon-box {
  position: fixed;
  bottom: 30px;   /* 화면 아래에서 30px */
  right: 30px;    /* 화면 오른쪽에서 30px */
  width: 220px;   /* 카드 크기 */
  background-color: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 2000;  /* 다른 요소 위에 표시 */
}
.review-reply {
  margin: 10px 0 0 50px;
  padding: 10px 15px;
  border-left: 3px solid #f39c12;
  background: #fffbea;
  border-radius: 5px;
  font-size: 0.9rem;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  color: #555;
}

.reply-author {
  font-weight: bold;
  color: #d35400;
}

.reply-text {
  white-space: pre-line;
}
  .coupon-box h4 { margin: 0 0 15px 0; font-size: 1.2rem; font-weight: 600; }
  .coupon-box h4 strong { color: #007bff; }
  .btn-download { background-color: #007bff; color: white; border: none; border-radius: 8px; padding: 12px; width: 100%; font-size: 1rem; font-weight: 700; cursor: pointer; margin-bottom: 10px; }
  .coupon-dropdown { color: #555; text-decoration: none; font-size: 0.9rem; }
  .amenities-preview-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 2개씩 */
  gap: 6px 12px; /* 행/열 간격 */
  font-size: 0.9rem;
  color: #555;
}

.more-link {
  font-size: 0.9rem;
  color: #007bff;
  cursor: pointer;
  margin-left: auto;
}
.reviews-empty-box {
  background-color: #f5f5f5;   /* 연한 회색 배경 */
  color: #666;                 /* 회색 글씨 */
  text-align: center;          /* 가운데 정렬 */
  padding: 50px 20px;          /* 넉넉한 여백 */
  font-size: 1.1rem;           /* 글씨 조금 키움 */
  font-weight: 500;
  border-radius: 8px;          /* 모서리 둥글게 */
}
</style>
