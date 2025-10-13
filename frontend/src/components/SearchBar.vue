<template>
  <section class="search-section" ref="searchSectionRef">
    <div class="content-wrapper">

      <!-- 회색 직사각형: 검색바 전체를 감싸는 표면 -->
      <div class="search-surface">
        <!-- 메인 검색바 -->
        <form class="main-search-bar" ref="barRef" @submit.prevent="search">
          <!-- 1행: 목적지 (아이콘 없음) -->
          <div
            class="search-input-group destination"
            ref="destinationGroup"
            @click.stop="openDestinationPanelAndFocus"
          >
            <input
              ref="destinationInput"
              type="text"
              placeholder="어디로 떠날까요?"
              v-model="destination"
              @focus="openDestinationPanel"
              @input="onDestinationInput"
              @keydown.esc="clearDestination"
            />
            <button
              v-if="destination"
              type="button"
              class="clear-btn"
              @mousedown.prevent
              @click.stop="clearDestination"
              aria-label="입력 지우기"
            >×</button>
          </div>

          <!-- 2행: 날짜 (왼쪽) -->
          <div class="search-input-group dates" ref="datesBtn" @click.stop="toggleCalendar">
            <div class="date-box">
              <div class="date-main">{{ checkInMain }}</div>
              <div class="date-sub">{{ checkInDow }}</div>
            </div>

            <div class="date-divider"></div>

            <div class="date-box">
              <div class="date-main">{{ checkOutMain }}</div>
              <div class="date-sub">{{ checkOutDow }}</div>
            </div>
          </div>

          <!-- 2행: 인원/객실 (오른쪽) -->
          <div class="search-input-group guests" ref="guestBtn" @click.stop="toggleGuestSelector">
            <span class="guests-main">{{ guestsSummary }}</span>
          </div>

          <!-- 3행: 검색 버튼 (가운데) -->
          <button type="submit" class="search-button">검색하기</button>
        </form>
      </div>

      <!-- 목적지 추천 패널 -->
      <teleport to="body">
        <div
          v-if="isDestinationOpen"
          class="sb-popover dest-suggest"
          :style="{ top: destPos.top + 'px', left: destPos.left + 'px', width: destPos.width + 'px' }"
        >
          <div class="dest-tabs">
            <button
              type="button"
              class="tab"
              :class="{ active: destTab === 'stay' }"
              @click="destTab = 'stay'"
            >숙박</button>
          </div>

          <div class="dest-header">대한민국 내 여행지</div>
          <ul class="dest-list">
            <li v-for="d in filteredDestinations" :key="d.name">
              <button type="button" class="dest-row" @click.stop="pickDestination(d)">
                <div class="dest-title">
                  <span class="name">{{ d.name }}</span>
                  <span class="count">({{ d.count.toLocaleString() }})</span>
                </div>
                <div class="dest-tags">{{ d.tags.join(', ') }}</div>
              </button>
            </li>
            <li v-if="filteredDestinations.length === 0" class="dest-empty">
              일치하는 추천지가 없습니다.
            </li>
          </ul>
        </div>
      </teleport>

      <!-- 달력 팝업 -->
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

      <!-- 인원/객실 팝업 -->
      <teleport to="body">
        <div
          v-if="isGuestSelectorOpen"
          class="sb-popover guest-selector-popup"
          :style="{ top: guestPos.top + 'px', left: guestPos.left + 'px' }"
          @click.stop
        >
          <div class="guest-row" v-for="guest in guestTypes" :key="guest.type">
            <div class="guest-labels">
              <span class="label">{{ guest.label }}</span>
            </div>
            <div class="counter">
              <button type="button" @click.stop="decrement(guest.type)" :disabled="getValue(guest.type) <= guest.min">−</button>
              <span>{{ getValue(guest.type) }}</span>
              <button type="button" @click.stop="increment(guest.type)" :disabled="getValue(guest.type) >= guest.max">+</button>
            </div>
          </div>
          <div class="guest-actions">
            <button type="button" class="confirm-btn" @click="confirmGuests">확인</button>
          </div>
        </div>
      </teleport>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

/* ===== 상태 ===== */
const searchSectionRef = ref(null);
const barRef = ref(null);

const destination = ref('');
const destinationGroup = ref(null);
const destinationInput = ref(null);
const isDestinationOpen = ref(false);
const destTab = ref('stay');
const destPos = reactive({ top: 0, left: 0, width: 0 });

const isCalendarOpen = ref(false);
const datesBtn = ref(null);
const calPos = reactive({ top: 0, left: 0, width: 0 });

const isGuestSelectorOpen = ref(false);
const guestBtn = ref(null);
const guestPos = reactive({ top: 0, left: 0 });

const checkIn = ref(null);
const checkOut = ref(null);

const rooms = ref(1);
const persons = ref(2);

const router = useRouter();
const route = useRoute();


// ✅ 타임존 문제없는 날짜 포맷 함수 추가
const formatDateToYYYYMMDD = (date) => {
  if (!date) return null;
  // getFullYear, getMonth, getDate는 로컬 시간대를 기준으로 값을 반환합니다.
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/* 요일 배열 */
const DOW = ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'];

const guestTypes = [
  { type: 'rooms',   label: '객실', min: 1, max: 5 },
  { type: 'persons', label: '인원', min: 1, max: 20 },
];

/* 오늘/내일 계산(00:00 고정) */
const todayBase = new Date(); todayBase.setHours(0,0,0,0);
const tomorrowBase = new Date(todayBase); tomorrowBase.setDate(tomorrowBase.getDate() + 1);

onMounted(() => {
  // URL 쿼리 우선
  if (route.query.region) destination.value = route.query.region;
  if (route.query.startDate) checkIn.value = new Date(route.query.startDate);
  if (route.query.endDate) checkOut.value = new Date(route.query.endDate);
  if (route.query.rooms) rooms.value = parseInt(route.query.rooms);
  if (route.query.persons) persons.value = parseInt(route.query.persons);

  // 쿼리가 없다면 기본값: 오늘 / 내일
  if (!checkIn.value)  checkIn.value  = new Date(todayBase);
  if (!checkOut.value) checkOut.value = new Date(tomorrowBase);
});

/* 팝업 닫기/외부클릭 */
const closeAllPopups = () => {
  isCalendarOpen.value = false;
  isGuestSelectorOpen.value = false;
  isDestinationOpen.value = false;
};
const handleClickOutside = (e) => {
  if (e.target.closest('.sb-popover')) return;
  if (searchSectionRef.value && !searchSectionRef.value.contains(e.target)) {
    closeAllPopups();
  }
};
onMounted(() => document.addEventListener('click', handleClickOutside, true));
onUnmounted(() => document.removeEventListener('click', handleClickOutside, true));

/* 위치 계산 */
const rectToPos = (el, extraBottom = 8, fullWidth = false) => {
  const r = el.getBoundingClientRect();
  return { top: Math.round(r.bottom + extraBottom), left: Math.round(r.left), width: fullWidth ? Math.round(r.width) : undefined };
};
const updateAllPositions = () => {
  if (isDestinationOpen.value && destinationGroup.value) {
    const p = rectToPos(destinationGroup.value, 8, true);
    destPos.top = p.top; destPos.left = p.left; destPos.width = p.width;
  }
  if (isCalendarOpen.value && barRef.value) {
    const p = rectToPos(barRef.value, 12, true);
    calPos.top = p.top; calPos.left = p.left; calPos.width = p.width;
  }
  if (isGuestSelectorOpen.value && guestBtn.value) {
    const p = rectToPos(guestBtn.value, 6, false);
    guestPos.top = p.top; guestPos.left = p.left;
  }
};
onMounted(() => {
  window.addEventListener('scroll', updateAllPositions, true);
  window.addEventListener('resize', updateAllPositions, true);
});
onUnmounted(() => {
  window.removeEventListener('scroll', updateAllPositions, true);
  window.removeEventListener('resize', updateAllPositions, true);
});

/* 목적지 패널 */
const openDestinationPanel = async () => {
  isCalendarOpen.value = false;
  isGuestSelectorOpen.value = false;
  isDestinationOpen.value = true;
  await nextTick();
  updateAllPositions();
};
const openDestinationPanelAndFocus = async () => {
  await openDestinationPanel();
  destinationInput.value?.focus();
};
const onDestinationInput = () => { if (!isDestinationOpen.value) openDestinationPanel(); };
const pickDestination = (d) => { destination.value = d.name; isDestinationOpen.value = false; };
const clearDestination = () => { destination.value = ''; nextTick(() => destinationInput?.value?.focus?.()); };

/* 달력 */
const toggleCalendar = async () => {
  isGuestSelectorOpen.value = false;
  isDestinationOpen.value = false;
  isCalendarOpen.value = !isCalendarOpen.value;
  await nextTick();
  updateAllPositions();
};
const weekDays = ['일','월','화','수','목','금','토'];
const today = new Date(); today.setHours(0,0,0,0);
const hoveredDate = ref(null);

const generateMonths = (numMonths = 2, offset = 0) => {
  const monthsArray = [];
  for (let i = 0; i < numMonths; i++) {
    const monthDate = new Date(today.getFullYear(), today.getMonth() + offset + i, 1);
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
const monthOffset = ref(0);
const months = ref(generateMonths(2, monthOffset.value));
const updateMonths = () => { months.value = generateMonths(2, monthOffset.value); };
const prevMonth = () => { monthOffset.value -= 1; updateMonths(); };
const nextMonth = () => { monthOffset.value += 1; updateMonths(); };

const selectDate = (y, m, d) => {
  const date = new Date(y, m - 1, d); date.setHours(0,0,0,0);
  if (!checkIn.value || (checkIn.value && checkOut.value)) {
    checkIn.value = date; checkOut.value = null;
  } else {
    if (date > checkIn.value) checkOut.value = date;
    else { checkOut.value = checkIn.value; checkIn.value = date; }
  }
};
const isDisabled = (y, m, d) => {
  const date = new Date(y, m - 1, d);
  date.setHours(0,0,0,0);
  return date < today;
};
const isSelectedDate = (y, m, d) => {
  const date = new Date(y, m - 1, d); date.setHours(0,0,0,0);
  return (checkIn.value && date.getTime() === checkIn.value.getTime()) ||
         (checkOut.value && date.getTime() === checkOut.value.getTime());
};
const isInRange = (y, m, d) => {
  if (!checkIn.value || !checkOut.value) return false;
  const date = new Date(y, m - 1, d); date.setHours(0,0,0,0);
  return date > checkIn.value && date < checkOut.value;
};
const isPreviewRange = (y, m, d) => {
  if (!checkIn.value || checkOut.value || !hoveredDate.value) return false;
  const date = new Date(y, m - 1, d); date.setHours(0,0,0,0);
  const start = checkIn.value; const end = hoveredDate.value > start ? hoveredDate.value : null;
  return !!end && date > start && date < end;
};
const onDayHover = (y, m, d) => {
  const date = new Date(y, m - 1, d); date.setHours(0,0,0,0);
  if (date < today) return;
  if (!checkIn.value || checkOut.value) { hoveredDate.value = null; return; }
  hoveredDate.value = date;
};
const onDayHoverEnd = () => { hoveredDate.value = null; };

/* 인원/객실 */
const getValue = (type) => (type === 'rooms' ? rooms.value : persons.value);
const setValue = (type, v) => { if (type === 'rooms') rooms.value = v; else persons.value = v; };
const increment = (type) => { const meta = guestTypes.find(g => g.type === type); const cur = getValue(type); if (cur < meta.max) setValue(type, cur + 1); };
const decrement = (type) => { const meta = guestTypes.find(g => g.type === type); const cur = getValue(type); if (cur > meta.min) setValue(type, cur - 1); };
const confirmGuests = () => { closeAllPopups(); };
const guestsSummary = computed(() => `성인 ${persons.value}명 · 객실 ${rooms.value}개`);

/* 목적지 추천 데이터 */
const destinations = ref([
  { name: '강릉', count: 1180, tags: ['스키', '해변'] },
  { name: '서울', count: 5945, tags: ['쇼핑', '레스토랑'] },
  { name: '인천', count: 2147, tags: ['관광'] },
  { name: '부산', count: 2734, tags: ['해변', '레스토랑'] },
  { name: '속초', count: 800,  tags: ['해변', '레스토랑'] },
  { name: '제주', count: 4939, tags: ['자연경관', '해변'] },
]);
const filteredDestinations = computed(() => {
  const q = (destination.value || '').trim().toLowerCase();
  if (!q) return destinations.value;
  return destinations.value.filter(d =>
    d.name.toLowerCase().includes(q) || d.tags.some(t => t.toLowerCase().includes(q))
  );
});

/* 날짜 텍스트 (두 줄) */
const fmtDateMain = (d) => `${d.getFullYear()}년 ${d.getMonth()+1}월 ${d.getDate()}일`;
const fmtDow = (d) => DOW[d.getDay()];
const checkInMain  = computed(() => checkIn.value  ? fmtDateMain(checkIn.value)  : fmtDateMain(todayBase));
const checkOutMain = computed(() => checkOut.value ? fmtDateMain(checkOut.value) : fmtDateMain(tomorrowBase));
const checkInDow   = computed(() => checkIn.value  ? fmtDow(checkIn.value)  : fmtDow(todayBase));
const checkOutDow  = computed(() => checkOut.value ? fmtDow(checkOut.value) : fmtDow(tomorrowBase));

/* 토글 */
const toggleGuestSelector = async () => {
  isCalendarOpen.value = false;
  isDestinationOpen.value = false;
  isGuestSelectorOpen.value = !isGuestSelectorOpen.value;
  await nextTick();
  updateAllPositions();
};

/* 검색 */
const search = async () => {
  if (!destination.value || destination.value.trim() === '') return alert('목적지를 입력해주세요.');
  if (!checkIn.value || !checkOut.value) return alert('체크인/체크아웃 날짜를 선택해주세요.');
  if (!rooms.value || rooms.value <= 0) return alert('객실 수는 1개 이상이어야 합니다.');
  if (!persons.value || persons.value <= 0) return alert('인원 수는 1명 이상이어야 합니다.');

  router.push({
    path: '/search',
    query: {
      region: destination.value,
      startDate: formatDateToYYYYMMDD(checkIn.value),
      endDate: formatDateToYYYYMMDD(checkOut.value),
      rooms: rooms.value,
      persons: persons.value,
    }
  });
};
</script>

<style scoped>
/* ===== 컨테이너 ===== */
.search-section{ position:relative; z-index:5; }
.content-wrapper{ max-width:1200px; margin:0 auto; padding:24px 16px 0; box-sizing:border-box; }

/* 회색 직사각형(이미지 레퍼런스) */
.search-surface{
  background:#f5f6f8;               /* 연회색 */
  border:1px solid #e7e9ee;         /* 옅은 테두리 */
  border-radius:16px;               /* 둥근 모서리 */
  padding:16px;                     /* 안쪽 여백 */
  box-shadow:0 18px 40px rgba(0,0,0,.08); /* 은은한 그림자 */
}

/* ===== 레이아웃: 1행 목적지 / 2행 날짜+게스트 / 3행 버튼 ===== */
.main-search-bar{
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-areas:
    "dest   dest   dest"
    "dates  guests guests"
    "submit submit submit";
  row-gap: 12px;
  align-items: center;
  background:#fff;                  /* 내부는 흰색 카드 */
  border:1px solid #e5e7eb;
  border-radius:12px;
  box-shadow:none;
  padding: 12px;
}

.search-input-group{
  display:flex; align-items:center; gap:14px;
  min-height:64px;                  /* 살짝 높게 */
  padding:16px 18px;
  border-right:0;
}
.search-input-group input{ border:none; outline:none; width:100%; font-size:16px; color:#111827; }

/* 영역 배치 */
.search-input-group.destination{ grid-area: dest; border-bottom:1px solid #f0f2f4; position:relative; }
.search-input-group.dates{ grid-area: dates; border-right:1px solid #f0f2f4; }
.search-input-group.guests{ grid-area: guests; }

/* 날짜 UI (두 줄) */
.dates{ display:flex; align-items:center; gap:18px; }
.date-box{ display:flex; flex-direction:column; line-height:1.2; }
.date-main{ font-weight:700; color:#111827; font-size:15px; }
.date-sub{  font-size:13px; color:#6b7280; margin-top:4px; }
.date-divider{ width:1px; height:36px; background:#e5e7eb; align-self:center; }

/* 인원/객실 텍스트 */
.guests-main{ font-weight:600; color:#111827; }

/* 입력 지우기 버튼 */
.clear-btn{
  position:absolute; right:12px; top:50%; transform:translateY(-50%);
  width:28px; height:28px; border:1px solid rgba(148,163,184,.55);
  border-radius:50%; background:#fff;
  display:grid; place-items:center; cursor:pointer;
  transition: box-shadow .2s ease, transform .08s ease, border-color .2s ease;
  line-height:1; color:#475569; font-size:16px;
}
.clear-btn:hover{ box-shadow:0 6px 16px rgba(15,23,42,.12); border-color:#94a3b8; }
.clear-btn:active{ transform:translateY(-50%) scale(.96); }

/* 검색 버튼 */
.search-button{
  grid-area: submit;
  justify-self: center;
  align-self: center;
  width: clamp(260px, 42%, 480px);
  height: 54px;
  border-radius: 12px;
  background:#4f7bf7; color:#fff;   /* 이미지 느낌의 블루 */
  border:none; font-weight:800; cursor:pointer; font-size:16px;
  box-shadow:0 10px 20px rgba(79,123,247,.25);
  transition: transform .05s ease, box-shadow .2s ease;
}
.search-button:hover{ box-shadow:0 14px 26px rgba(79,123,247,.32); }
.search-button:active{ transform:translateY(1px); }

/* ===== 공통 팝오버 ===== */
.sb-popover{ position:fixed; z-index:1000; }

/* ===== 목적지 패널 ===== */
.dest-suggest {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, .14);
  padding: 14px 8px 16px;
}
.dest-tabs { display:flex; gap:8px; margin-bottom:10px; padding:0 8px; }
.dest-tabs .tab{
  padding:6px 12px; border-radius:999px; border:1px solid #e5e7eb; background:#fff; cursor:pointer; font-size:13px;
}
.dest-tabs .tab.active{ background:#111827; color:#fff; border-color:#111827; }
.dest-header{ font-weight:600; font-size:14px; color:#6b7280; margin:4px 10px 10px; }
.dest-list{
  list-style:none; margin:0; padding:0 8px 8px;
  display:grid; grid-template-columns: repeat(3, 1fr); gap:16px;
}
.dest-row{
  width:100%; text-align:left; padding:10px 12px; border-radius:10px; background:#fff;
  border:1px solid transparent; cursor:pointer;
}
.dest-row:hover{ background:#fafafa; border-color:#e5e7eb; }
.dest-title{ font-size:15px; font-weight:500; color:#111827; }
.dest-title .count{ margin-left:6px; font-weight:500; color:#6b7280; }
.dest-tags{ font-size:13px; color:#6b7280; margin-top:2px; }
.dest-empty{ padding:10px 12px; color:#6b7280; }
@media (max-width: 820px){ .dest-list{ grid-template-columns:1fr; } }

/* ===== 달력 ===== */
.calendar-popup{
  display:flex; gap:20px; background:#fff; border:1px solid #e5e7eb; border-radius:14px;
  padding:12px 20px 24px; box-shadow:0 18px 40px rgba(0,0,0,.14); width:auto; box-sizing:border-box;
}
.calendar-month{ width:50%; }
.month-name{ font-weight:700; margin-bottom:6px; color:#111827; }
.days-header{ color:#6b7280; font-size:12px; display:grid; grid-template-columns:repeat(7,1fr); gap:6px; }
.days-grid{ display:grid; grid-template-columns:repeat(7,1fr); gap:6px; margin-top:8px; }
.day{ display:inline-flex; align-items:center; justify-content:center; height:36px; border-radius:8px; cursor:pointer;
      transition: background .15s ease, color .15s ease, opacity .15s ease, box-shadow .15s ease; }
.day.empty{ visibility:hidden; }
.day.disabled{ opacity:.38; pointer-events:none; cursor:default; }
.day.disabled:hover{ box-shadow:none; }
.day:not(.disabled):hover{ box-shadow: inset 0 0 0 2px rgba(25,118,210,.25); }
.day.selected{ background:#1976d2; color:#fff; }
.day.inRange{ background:rgba(25,118,210,.18); }
.day.previewRange{ background:rgba(25,118,210,.12); box-shadow:inset 0 0 0 1px rgba(25,118,210,.2); }
.calendar-nav{
  position:absolute; top:10px; width:36px; height:36px; border:none; border-radius:50%; background:#fff;
  box-shadow:0 2px 8px rgba(0,0,0,.15); font-size:22px; line-height:36px; text-align:center; cursor:pointer; user-select:none; z-index:2;
}
.calendar-nav.prev{ left:10px; } .calendar-nav.next{ right:10px; }

/* ===== 게스트 ===== */
.guest-selector-popup{
  width:280px; background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px 0;
  box-shadow:0 18px 40px rgba(0,0,0,.14);
}
.guest-row{ display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; padding:0 16px; }
.guest-labels{ display:flex; flex-direction:column; }
.label{ font-size:15px; font-weight:600; color:#111827; }
.counter{ display:flex; align-items:center; gap:14px; }
.counter button{
  width:34px; height:34px; border-radius:50%; border:1px solid #cbd5e1; background:#fff; font-size:18px; font-weight:700;
  cursor:pointer; color:#111827;
}
.counter button:disabled{ color:#cbd5e1; cursor:not-allowed; }
.counter span{ font-size:16px; width:26px; text-align:center; color:#111827; }
.guest-actions{ padding:0 16px; }
.confirm-btn{ width:100%; height:40px; border-radius:10px; border:none; background:#111827; color:#fff; font-weight:700; cursor:pointer; }
</style>