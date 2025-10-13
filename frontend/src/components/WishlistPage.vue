<template>
  <div class="list-page-container">
    <div class="content-wrapper">
      <header class="page-header">
        <h1>나의 찜 목록</h1>
        <p>마음에 드는 숙소를 모아보고 여행 계획을 세워보세요.</p>
      </header>

      <div class="search-bar" ref="searchBarRef">
        <div class="search-input-group dates" ref="datesBtnRef" @click.stop="toggleCalendar">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" viewBox="0 0 16 16"><path d="M4 .5a.5.5 0 0 0-1 0V1H2a2 2 0 0 0-2 2v1h16V3a2 2 0 0 0-2-2h-1V.5a.5.5 0 0 0-1 0V1H4V.5zM16 14V5H0v9a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2zM8 7.954a.236.236 0 0 1 .236-.236h.015a.236.236 0 0 1 .236.236v.015a.236.236 0 0 1-.236.236h-.015a.236.236 0 0 1-.236-.236V7.954z"/></svg>
          <span class="date-text">{{ checkInText }}</span>
          <span class="date-separator">-</span>
          <span class="date-text">{{ checkOutText }}</span>
        </div>
        <label>
          <span>객실</span>
          <input type="number" v-model.number="rooms" min="1" />
        </label>
        <label>
          <span>인원</span>
          <input type="number" v-model.number="persons" min="1" />
        </label>
        <button @click="searchWishlist" class="btn-search">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
            <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
          </svg>
          <span>예약 가능 숙소 조회</span>
        </button>
      </div>

      <teleport to="body">
        <div
          v-if="isCalendarOpen"
          class="sb-popover calendar-popup"
          :style="{ top: calPos.top + 'px', left: calPos.left + 'px', width: calPos.width + 'px' }"
          @click.stop
        >
          <button type="button" class="calendar-nav prev" @click.stop="prevMonth" aria-label="이전 달">‹</button>
          <button type="button" class="calendar-nav next" @click.stop="nextMonth" aria-label="다음 달">›</button>

          <div v-for="(month, index) in months" :key="index" class="calendar-month">
            <div class="month-name">{{ month.name }}</div>
            <div class="days-header">
              <span v-for="day in weekDays" :key="day">{{ day }}</span>
            </div>
            <div class="days-grid">
              <span v-for="empty in month.emptyDays" :key="'e' + empty" class="day empty"></span>
              <span
                v-for="day in month.days"
                :key="day"
                class="day"
                :class="{
                  selected: isSelectedDate(month.year, month.month, day),
                  inRange: isInRange(month.year, month.month, day),
                  previewRange: isPreviewRange(month.year, month.month, day),
                  disabled: isDisabled(month.year, month.month, day),
                }"
                :aria-disabled="isDisabled(month.year, month.month, day)"
                @click="!isDisabled(month.year, month.month, day) && selectDate(month.year, month.month, day)"
                @mouseenter="onDayHover(month.year, month.month, day)"
                @mouseleave="onDayHoverEnd"
              >
                {{ day }}
              </span>
            </div>
          </div>
        </div>
      </teleport>

      <div v-if="wishlistItems.length > 0" class="accommodations-grid">
        <div
          v-for="place in wishlistItems"
          :key="place.hId"
          class="reco-card"
          :class="{ disabled: place.minPrice <= 0 }"
          @click="goToHotelDetail(place)"
        >
        <img :src="place.image || gyeongjuImage" :alt="place.hName" class="reco-card-image" />
        <div class="reco-card-overlay"></div>
          <div class="reco-card-content">
            <span class="reco-card-location">{{ place.region }}</span>
            <h4>{{ place.hName }}</h4>
            <div class="review-info">
              <template v-if="place.reviewCount > 0">
                <span class="review-score">
                  ★ {{ place.avgScore?.toFixed(1) ?? 'N/A' }}
                  <span class="review-count"> ({{ place.reviewCount }}개)</span>
                </span>
              </template>
              <template v-else>
                <span class="no-review">아직 리뷰가 없어요</span>
              </template>
            </div>
          </div>
          <div class="price-overlay">
            <p class="min-price-info">
              <span v-if="place.minPrice > 0">1박 최저가<br/> <strong>{{ place.minPrice?.toLocaleString() }}원</strong></span>
              <span v-else class="sold-out">예약 마감</span>
            </p>
          </div>
          <button
            class="like-button liked"
            @click.stop="removeFromWishlist(place.hId)"
            aria-label="찜하기 취소"
          >
            <span class="heart-icon filled">❤️</span>
          </button>
        </div>
      </div>

      <div v-else class="empty-wishlist">
        <p>찜한 숙소가 아직 없습니다.</p>
        <router-link to="/" class="find-stay-btn">마음에 드는 숙소 찾아보기</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, watch, onMounted, computed, reactive, nextTick, onUnmounted } from "vue";
  import { useRouter } from "vue-router";
  import axios from "axios";
  import gyeongjuImage from '@/assets/images/card-gyeongju.jpg';

  const wishlistItems = ref([]);
  const token = localStorage.getItem("accessToken");
  const router = useRouter();

  const rooms = ref(1);
  const persons = ref(2);

  const searchBarRef = ref(null);
  const datesBtnRef = ref(null);
  const isCalendarOpen = ref(false);
  const calPos = reactive({ top: 0, left: 0, width: 0 });
  
  const today = new Date();
  today.setHours(0,0,0,0);
  
  const checkInDate = new Date();
  const checkOutDate = new Date();
  checkOutDate.setDate(checkInDate.getDate() + 1);

  const checkIn = ref(checkInDate);
  const checkOut = ref(checkOutDate);

  const fetchWishlist = async () => {
    if (!token) return;
    try {
      const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/wishlist`, {
        headers: { Authorization: `Bearer ${token}` },
        params: {
          checkIn: checkIn.value.toISOString().split('T')[0],
          checkOut: checkOut.value.toISOString().split('T')[0],
          rooms: rooms.value,
          persons: persons.value
        }
      });

      wishlistItems.value = response.data.map(item => ({
        ...item,
        hId: item.hId, // id 통일
        hName: item.hName,
        // ✅ 이미지 경로 직접 생성
        image: `${import.meta.env.VITE_APP_API_URL}/images/${item.type}/${item.hId}.jpg`
      }));
    } catch (error) {
      console.error("찜 목록 불러오기 실패", error);
    }
  };

  const goToHotelDetail = (place) => {
  if (!place.minPrice || place.minPrice <= 0) {
    return; // 객실 없으면 이동 X
  }

  router.push({
    path: `/hotel/${place.hId}`,
      query: {
        region: place.region,
        startDate: checkIn.value.toISOString().split('T')[0],
        endDate: checkOut.value.toISOString().split('T')[0],
        rooms: rooms.value,
        persons: persons.value
      }
    });
  };

  const removeFromWishlist = async (hid) => {
    if (!token || !hid) return;
    try {
      await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/wishlist/${hid}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      wishlistItems.value = wishlistItems.value.filter(item => item.hId !== hid);
      alert('찜 목록에서 해제되었습니다.');
    } catch (error) {
      console.error('찜 해제 실패', error);
    }
  };

  watch([checkIn, checkOut, rooms, persons], fetchWishlist);
  const searchWishlist = () => fetchWishlist();
  onMounted(fetchWishlist);

  const closeAllPopups = () => isCalendarOpen.value = false;

  const handleClickOutside = (e) => {
    if (e.target.closest('.sb-popover')) return;
    if (searchBarRef.value && !searchBarRef.value.contains(e.target)) {
      closeAllPopups();
    }
  };

  const updatePosition = () => {
    if (isCalendarOpen.value && datesBtnRef.value) {
      const rect = searchBarRef.value.getBoundingClientRect();
      calPos.top = rect.bottom + 8;
      calPos.left = rect.left;
      calPos.width = rect.width;
    }
  };
  
  onMounted(() => {
    document.addEventListener('click', handleClickOutside);
    window.addEventListener('scroll', updatePosition, true);
    window.addEventListener('resize', updatePosition, true);
  });
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside);
    window.removeEventListener('scroll', updatePosition, true);
    window.removeEventListener('resize', updatePosition, true);
  });

  const toggleCalendar = async () => {
    isCalendarOpen.value = !isCalendarOpen.value;
    await nextTick();
    updatePosition();
  };

  const weekDays = ['일','월','화','수','목','금','토'];
  const hoveredDate = ref(null);
  const monthOffset = ref(0);

  const generateMonths = (numMonths = 2, offset = 0) => {
    const monthsArray = [];
    const baseDate = new Date(today.getFullYear(), today.getMonth() + offset, 1);
    for (let i = 0; i < numMonths; i++) {
      const monthDate = new Date(baseDate.getFullYear(), baseDate.getMonth() + i, 1);
      const year = monthDate.getFullYear();
      const month = monthDate.getMonth() + 1;
      const monthName = `${year}년 ${month}월`;
      const daysInMonth = new Date(year, month, 0).getDate();
      const firstDay = new Date(year, month - 1, 1).getDay();
      monthsArray.push({
        year, month, name: monthName,
        days: Array.from({ length: daysInMonth }, (_, k) => k + 1),
        emptyDays: Array.from({ length: firstDay }, (_, k) => k),
      });
    }
    return monthsArray;
  };
  
  const months = ref(generateMonths(2, monthOffset.value));
  const updateMonths = () => { months.value = generateMonths(2, monthOffset.value); };
  const prevMonth = () => { monthOffset.value -= 1; updateMonths(); };
  const nextMonth = () => { monthOffset.value += 1; updateMonths(); };

  const selectDate = (y, m, d) => {
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    if (!checkIn.value || (checkIn.value && checkOut.value)) {
      checkIn.value = date;
      checkOut.value = null;
    } else {
      if (date > checkIn.value) {
        checkOut.value = date;
        closeAllPopups();
      } else {
        checkOut.value = checkIn.value;
        checkIn.value = date;
      }
    }
  };

  const isDisabled = (y, m, d) => {
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    return date < today;
  };

  const checkInText = computed(() =>
    checkIn.value ? `${checkIn.value.getMonth() + 1}월 ${checkIn.value.getDate()}일` : '체크인'
  );
  const checkOutText = computed(() =>
    checkOut.value ? `${checkOut.value.getMonth() + 1}월 ${checkOut.value.getDate()}일` : '체크아웃'
  );

  const isSelectedDate = (y, m, d) => {
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    return (checkIn.value && date.getTime() === checkIn.value.getTime()) ||
           (checkOut.value && date.getTime() === checkOut.value.getTime());
  };
  const isInRange = (y, m, d) => {
    if (!checkIn.value || !checkOut.value) return false;
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    return date > checkIn.value && date < checkOut.value;
  };
  const isPreviewRange = (y, m, d) => {
    if (!checkIn.value || checkOut.value || !hoveredDate.value) return false;
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    const start = checkIn.value;
    const end = hoveredDate.value > start ? hoveredDate.value : null;
    return !!end && date > start && date < end;
  };

  const onDayHover = (y, m, d) => {
    const date = new Date(y, m - 1, d);
    date.setHours(0,0,0,0);
    if (date < today || !checkIn.value || checkOut.value) {
      hoveredDate.value = null;
      return;
    }
    hoveredDate.value = date;
  };

  const onDayHoverEnd = () => { hoveredDate.value = null; };
</script>

<style scoped>
  /* --- 페이지 기본 스타일 --- */
  .list-page-container { padding: 60px 20px; background-color: #f8f9fa; min-height: 80vh; }
  .content-wrapper { max-width: 1280px; margin: 0 auto; }
  .page-header { text-align: center; margin-bottom: 50px; }
  .page-header h1 { font-size: 2.5rem; font-weight: 700; color: #333; }
  .page-header p { font-size: 1.1rem; color: #666; }

  /* --- 검색 바 스타일 --- */
  .search-bar {
    display: flex; gap: 1.5rem; align-items: flex-end; justify-content: center;
    margin-bottom: 40px; padding: 25px; background-color: white; border-radius: 16px;
    flex-wrap: wrap; box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05); border: 1px solid #E5E5E5;
  }
  .search-bar label { display: flex; flex-direction: column; font-weight: 500; color: #555; gap: 8px; }
  .search-bar input {
    padding: 12px; border-radius: 8px; border: 1px solid #ddd;
    font-size: 1rem; min-width: 150px;
  }
  .search-input-group.dates {
    display: flex; align-items: center; gap: 10px;
    padding: 12px; border: 1px solid #ddd; border-radius: 8px; cursor: pointer;
  }
  .date-text { font-weight: 600; }
  .date-separator { color: #aaa; }
  .search-bar .btn-search {
    padding: 12px 24px; background-color: #0A2A66; color: white; border: none;
    border-radius: 8px; font-weight: 600; cursor: pointer; transition: all 0.2s;
    display: inline-flex; align-items: center; gap: 8px;
  }
  .search-bar .btn-search:hover {
    background-color: #082255; box-shadow: 0 4px 15px rgba(10, 42, 102, 0.2);
    transform: translateY(-2px);
  }

  /* --- 찜 목록 없음 --- */
  .empty-wishlist { text-align: center; padding: 80px 50px; background-color: white; border-radius: 12px; }
  .empty-wishlist p { font-size: 1.2rem; color: #888; margin: 0 0 25px 0; }
  .find-stay-btn {
    display: inline-block; padding: 12px 24px; background-color: #0A2A66; color: white;
    border-radius: 8px; text-decoration: none; font-weight: bold; transition: all 0.2s;
  }
  .find-stay-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(10, 42, 102, 0.2); }

  /* --- 숙소 카드 --- */
  .accommodations-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 30px; }
  .reco-card {
    position: relative; border-radius: 16px; overflow: hidden; height: 380px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05); transition: all 0.3s; cursor: pointer;
  }
  .reco-card:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1); }
  .reco-card.disabled { cursor: not-allowed; }
  .reco-card.disabled .reco-card-image { filter: grayscale(80%); }
  .reco-card-image { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s ease; }
  .reco-card:hover .reco-card-image { transform: scale(1.05); }
  .reco-card-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, transparent 100%); }
  .reco-card-content { position: absolute; bottom: 0; left: 0; padding: 25px; color: white; width: 100%; z-index: 2; }
  .reco-card-location { font-size: 0.9em; font-weight: 500; }
  .reco-card-content h4 { font-size: 1.35rem; margin: 5px 0 8px; font-weight: 700; color: #fff; }
  .review-info { font-size: 0.9em; }
  .review-score { color: #FFD700; font-weight: 700; }
  .review-count, .no-review { color: #fff; opacity: 0.9; }
  .price-overlay { position: absolute; bottom: 25px; right: 25px; color: white; text-align: right; z-index: 2; }
  .min-price-info { font-size: 1.05rem; font-weight: 400; margin: 0; }
  .min-price-info strong { font-size: 1.3rem; font-weight: 800; }
  .sold-out { font-size: 1.2rem; font-weight: 700; color: #FF6B6B; }
  .like-button {
    position: absolute; top: 15px; right: 15px; background-color: rgba(255, 255, 255, 0.2); backdrop-filter: blur(5px);
    border: 1px solid rgba(255, 255, 255, 0.3); border-radius: 50%; width: 40px; height: 40px;
    display: flex; justify-content: center; align-items: center; cursor: pointer; padding: 0; z-index: 3; transition: all 0.2s;
  }
  .like-button:hover { transform: scale(1.1); background-color: rgba(255, 255, 255, 0.3); }
  .heart-icon { font-size: 1.5rem; }
  .heart-icon.filled { color: #FF3B30; text-shadow: 0 0 8px rgba(255, 59, 48, 0.7); }

  /* --- 달력 팝업 --- */
  .sb-popover { position: fixed; z-index: 1000; }
  .calendar-popup {
    display: flex; gap: 30px; background: #fff; border: 1px solid #e5e7eb; border-radius: 14px;
    padding: 24px; box-shadow: 0 18px 40px rgba(0, 0, 0, .14); box-sizing: border-box;
  }
  .calendar-month { width: 300px; }
  .month-name { font-size: 1.1rem; font-weight: 700; margin-bottom: 16px; text-align: center; color: #111827; }
  .days-header, .days-grid { display: grid; grid-template-columns: repeat(7, 1fr); }
  .days-header span { text-align: center; color: #6b7280; font-size: 0.9rem; font-weight: 600; padding-bottom: 8px; }
  .day {
    display: inline-flex; align-items: center; justify-content: center; height: 40px;
    border-radius: 50%; cursor: pointer; transition: all .2s ease;
  }
  .day.empty { visibility: hidden; }
  .day.disabled { opacity: .38; pointer-events: none; }
  .day:not(.disabled):hover { background-color: #f0f2f4; }
  .day.selected { background: #0A2A66; color: #fff; font-weight: 700; }
  .day.inRange { background: rgba(10, 42, 102, .1); border-radius: 0; }
  .day.previewRange { background: rgba(10, 42, 102, .05); border-radius: 0; }
  .calendar-nav {
    position: absolute; top: 20px; width: 36px; height: 36px; border: 1px solid #e5e7eb;
    border-radius: 50%; background: #fff; font-size: 20px; cursor: pointer; z-index: 2;
  }
  .calendar-nav.prev { left: 20px; }
  .calendar-nav.next { right: 20px; }
</style>