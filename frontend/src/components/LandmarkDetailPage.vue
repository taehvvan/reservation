<!-- LandmarkDetailPage.vue -->
<template>
  <!-- 1) 로딩 -->
  <div v-if="isLoading" class="loading-container">
    <p>데이터를 불러오는 중입니다...</p>
  </div>

  <!-- 2) 데이터 OK -->
  <div v-else-if="landmark" class="detail-page-container">
    <div class="content-wrapper">
      <main class="main-content-column">
        <!-- breadcrumbs -->
        <nav class="breadcrumbs" aria-label="Breadcrumb">
          <router-link to="/">홈</router-link>
          <span aria-hidden="true">›</span>
          <router-link to="/landmarks">랜드마크</router-link>
          <span aria-hidden="true">›</span>
          <span>{{ landmark.name }}</span>
        </nav>

        <!-- 갤러리: 이미지가 있을 때만 표시 -->
        <section v-if="gallery.length" class="gallery" aria-label="이미지 갤러리">
          <div
            class="hero-image"
            tabindex="0"
            @keydown.left.prevent="prev"
            @keydown.right.prevent="next"
          >
            <button class="nav prev" @click="prev" aria-label="이전 사진">‹</button>
            <img
              class="hero-img"
              :src="gallery[currentIndex]?.src"
              :alt="gallery[currentIndex]?.alt || landmark.name"
              decoding="async"
              fetchpriority="high"
              @error="onImgError"
            />
            <button class="nav next" @click="next" aria-label="다음 사진">›</button>
          </div>

          <div class="thumbs" ref="thumbsEl" role="listbox" aria-label="썸네일">
            <button
              v-for="(img, i) in gallery"
              :key="img.src + i"
              class="thumb"
              :class="{ active: i === currentIndex }"
              @click="go(i)"
              :aria-selected="i === currentIndex"
              :title="img.alt"
            >
              <img :src="img.src" :alt="img.alt" loading="lazy" decoding="async" @error="onImgError" />
            </button>
          </div>
        </section>

        <!-- 헤더 -->
        <div class="info-header">
          <div class="tags" v-if="landmark.tags?.length">
            <span v-for="tag in landmark.tags" :key="tag">{{ tag }}</span>
          </div>
          <h1>{{ landmark.name }}</h1>
          <p class="location">📍 {{ landmark.location }}</p>
        </div>

        <!-- 탭 -->
        <section class="info-card">
          <div class="tabs">
            <button :class="{active: tab==='basic'}" @click="tab='basic'">기본정보</button>
            <button :class="{active: tab==='guide'}" @click="tab='guide'">이용안내</button>
            <button :class="{active: tab==='detail'}" @click="tab='detail'">상세정보</button>
          </div>

          <table v-if="tab !== 'detail'" class="info-table">
            <tbody>
              <tr v-for="row in (tab==='basic' ? landmark.basic : landmark.guide)" :key="row.label">
                <th scope="row">{{ row.label }}</th>
                <td>{{ row.value }}</td>
              </tr>
            </tbody>
          </table>

          <div v-else class="detail-wrap">
            <div
              ref="detailRef"
              class="detail-text"
              :class="{ collapsed: !isDetailExpanded }"
            >
              <template v-if="detailIsHtml">
                <div v-html="detailDisplay"></div>
              </template>
              <template v-else>
                {{ detailDisplay }}
              </template>
            </div>

            <div v-if="!isDetailExpanded && isOverflow" class="fade" aria-hidden="true"></div>

            <div v-if="isOverflow" class="more-wrap">
              <button class="btn-more" @click="toggleExpand">
                {{ isDetailExpanded ? '접기' : '더보기' }}
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- 사이드 -->
      <aside class="sidebar-column">
        <div class="bottom-sidebar">
          <div class="nearby-hotel-card">
            <h3>근처 숙소 찾아보기</h3>
            <p>'{{ landmark.name }}' 근처의 멋진 숙소들을 둘러보세요.</p>
            <router-link
              :to="{ name: 'SearchResult', query: { destination: landmark.location } }"
              class="btn-find-hotels"
            >
              숙소 검색하기
            </router-link>
          </div>
        </div>
      </aside>
    </div>
  </div>

  <!-- 3) 비어있음 -->
  <div v-else class="loading-container">
    <p>데이터가 없습니다. 엑셀 파일 경로나 컬럼을 확인해 주세요.</p>
  </div>
</template>

<script setup>
import { ref, computed, watchEffect, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import * as XLSX from 'xlsx'

const route = useRoute()

/** 상태 */
const landmarks = ref([])
const landmark = ref(null)
const currentIndex = ref(0)
const tab = ref('detail') // 기본 탭: 상세정보
const thumbsEl = ref(null)
const isLoading = ref(true)

/** 상세 더보기 상태/측정 */
const detailRef = ref(null)
const isOverflow = ref(false)
const isDetailExpanded = ref(false)
const toggleExpand = () => { isDetailExpanded.value = !isDetailExpanded.value }

/** 유틸 */
// 쉼표/세미콜론/파이프만 구분자 (스페이스로는 split 안 함)
const splitList = (v) => {
  if (v == null) return []
  return String(v).split(/[,;|]+/).map(s => s.trim()).filter(Boolean)
}

// 엑셀이 `landmarks/...`를 주는 전제.
// 루트: `/landmarks/...` 로 매핑. (배포/개발 모두 public 아래 정적 서빙)
const resolveImage = (p) => {
  if (!p) return ''
  let s = String(p).trim()

  // 경로 정리
  s = s
    .replace(/\\/g, '/')                           // 역슬래시 -> 슬래시
    .replace(/[\u200B-\u200D\uFEFF]/g, '')        // 제로폭문자 제거
    .replace(/["'‘’“”]/g, '')                     // 따옴표 제거
    .replace(/[\u2010-\u2015\u2212\uFE58\uFE63\uFF0D]/g, '-') // 특수 대시 -> -
    .replace(/\/{2,}/g, '/')                       // // -> /
    .replace(/^\.?\/*/, '')                        // 앞의 ./, / 제거
    .replace(/^public\//i, '')                     // public/ 제거

  // 절대 URL은 그대로
  if (/^https?:\/\//i.test(s)) return s

  // 이미 루트 접두라면 정규화해서 반환
  if (/^\/?(landmarks|images)\//i.test(s)) {
    return s.startsWith('/') ? s : '/' + s
  }

  // 기본(엑셀이 상대경로일 때): /landmarks/ 아래에 있다고 가정
  return '/landmarks/' + s
}

// 깨진 이미지 placeholder (선택)
const onImgError = (e) => {
  e.target.onerror = null
  e.target.src = '/images/placeholder.png' // public/images/placeholder.png 준비해두면 깔끔
}

const normalizeTags = (arr) =>
  arr
    .map(t => t.startsWith('#') ? t : `#${t}`)
    .filter((t, i, a) => a.indexOf(t) === i)

// detail 안에 HTML 태그가 있는지 간단 감지
const hasHtml = (s) => /<\s*[a-z][\s\S]*>/i.test(s || '')

/** 한 행 → 랜드마크 객체 매핑 */
const mapRowToLandmark = (r, idx) => {
  const name = r.name || r.Name || r.이름 || r['명칭'] || '이름없음'

  // 이미지
  const imageList = splitList(r.images ?? r.Images ?? r.이미지 ?? r.이미지들 ?? '')
  const images = imageList.length
    ? imageList.map(src => ({ src: resolveImage(src), alt: `${name} 사진` }))
    : ((r.image || r.Image || r.대표이미지)
        ? [{ src: resolveImage(r.image || r.Image || r.대표이미지), alt: name }]
        : [])

  // 기본정보 / 이용안내
  const basic = []
  const guide = []
  if (r.basic_address || r.주소) basic.push({ label: '주소', value: r.basic_address || r.주소 })
  if (r.basic_homepage || r.홈페이지) basic.push({ label: '홈페이지', value: r.basic_homepage || r.홈페이지 })
  if (r.guide_phone || r.문의 || r.문의번호) guide.push({ label: '문의 및 안내', value: r.guide_phone || r.문의 || r.문의번호 })
  if (r.guide_closed || r.쉬는날) guide.push({ label: '쉬는날', value: r.guide_closed || r.쉬는날 })
  if (r.guide_hours || r.이용시간) guide.push({ label: '이용시간', value: r.guide_hours || r.이용시간 })

  // 엑셀 '개요'도 기본정보 표에 노출
  const overviewRaw = (r['개요'] ?? r.개요 ?? '').toString().trim()
  if (overviewRaw) basic.push({ label: '개요', value: overviewRaw })

  // 자유 확장: basic:라벨 / guide:라벨
  Object.keys(r).forEach(k => {
    const low = k.toLowerCase()
    if (low.startsWith('basic:')) basic.push({ label: k.slice(6).trim(), value: r[k] })
    if (low.startsWith('guide:')) guide.push({ label: k.slice(6).trim(), value: r[k] })
  })

  // 태그
  const tagFromTags = splitList(r.tags ?? r.Tags ?? r.태그 ?? '')
  const tagFromCategory = splitList(r['카테고리'] ?? r['category'] ?? '')
  const tags = normalizeTags([...tagFromTags, ...tagFromCategory])

  // 상세(개요)
  const detailRaw = (
    r.detail ?? r.Detail ?? r.상세 ?? r['상세정보'] ?? r['개요'] ??
    r.overview ?? r.Overview ?? r.소개 ?? r.description ?? r.Description ?? ''
  ).toString().trim()

  return {
    id: String(r.id ?? r.ID ?? r.아이디 ?? r.No ?? (idx + 1)),
    name,
    location: r.location ?? r.Location ?? r.지역 ?? r['주소'] ?? '',
    image: images[0]?.src || '',
    images,
    tags,
    description: (r.description ?? r.Description ?? r.소개 ?? '').toString().trim(),
    detail: detailRaw,
    basic,
    guide,
  }
}

/** 엑셀 로딩 (단일 파일) */
const fetchSheet = async (url) => {
  try {
    const res = await fetch(encodeURI(url))
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const buf = await res.arrayBuffer()
    const wb = XLSX.read(buf, { type: 'array' })
    const ws = wb.Sheets[wb.SheetNames[0]]
    return XLSX.utils.sheet_to_json(ws, { defval: '' })
  } catch (e) {
    console.warn('[엑셀 로딩 스킵]', url, e?.message || e)
    return []
  }
}

const DATA_FILE = '/data/landmarks.xlsx'

const loadExcel = async () => {
  isLoading.value = true
  try {
    const rows = await fetchSheet(DATA_FILE)
    landmarks.value = rows.map((r, i) => mapRowToLandmark(r, i)).filter(x => x.name)
  } finally {
    isLoading.value = false
  }
}

/** 초기 로딩 */
onMounted(loadExcel)

/** 라우트 id에 맞는 랜드마크 선택 */
watchEffect(() => {
  const id = String(route.params.id ?? '')
  const list = landmarks.value

  // 1) id(slug) 정확 일치
  const byStringId = list.find(x => String(x.id) === id)

  // 2) 숫자면 1-based 인덱스
  const byIndex = /^\d+$/.test(id) ? list[Number(id) - 1] : undefined

  landmark.value = byStringId ?? byIndex ?? list[0] ?? null

  currentIndex.value = 0
  isDetailExpanded.value = false
  nextTick(() => {
    thumbsEl.value?.scrollTo({ left: 0 })
    measureOverflow()
  })
  tab.value = (landmark.value?.detail?.trim() ? 'detail' : 'basic')
})

/** 갤러리 소스 */
const gallery = computed(() => {
  if (!landmark.value) return []
  return landmark.value.images?.length
    ? landmark.value.images
    : (landmark.value.image ? [{ src: landmark.value.image, alt: landmark.value.name }] : [])
})

/** 상세 HTML 여부 및 표시값 */
const detailIsHtml = computed(() => hasHtml(landmark.value?.detail))
const detailDisplay = computed(() => landmark.value?.detail || '')

/** 갤러리 이동 */
const go = (i) => {
  const len = gallery.value.length
  if (!len) return
  currentIndex.value = (i + len) % len
  scrollActiveThumbIntoView()
}
const prev = () => go(currentIndex.value - 1)
const next = () => go(currentIndex.value + 1)

const scrollActiveThumbIntoView = () => {
  const wrap = thumbsEl.value
  if (!wrap) return
  const active = wrap.querySelector('.thumb.active')
  if (!active) return
  const aw = active.offsetWidth
  const al = active.offsetLeft
  const vw = wrap.clientWidth
  const sl = wrap.scrollLeft
  if (al < sl) wrap.scrollTo({ left: al - 8, behavior: 'smooth' })
  else if (al + aw > sl + vw) wrap.scrollTo({ left: al - vw + aw + 8, behavior: 'smooth' })
}

/** 상세 탭 overflow 측정 */
const measureOverflow = () => {
  const el = detailRef.value
  if (!el) { isOverflow.value = false; return }
  const wasExpanded = isDetailExpanded.value
  isDetailExpanded.value = false
  nextTick(() => {
    isOverflow.value = el.scrollHeight > el.clientHeight + 1
    isDetailExpanded.value = wasExpanded
  })
}

watchEffect(() => {
  if (tab.value === 'detail') {
    nextTick(() => {
      // HTML 바인딩 완료 뒤 한 번 더 측정
      measureOverflow()
      requestAnimationFrame(measureOverflow)
    })
  }
})

const onResize = () => { if (tab.value === 'detail') measureOverflow() }
onMounted(() => window.addEventListener('resize', onResize))
onBeforeUnmount(() => window.removeEventListener('resize', onResize))
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;800&display=swap');

.detail-page-container { font-family: 'Noto Sans KR', sans-serif; background-color: #fff; padding: 40px 0; }
.content-wrapper { max-width: 1200px; margin: 0 auto; padding: 0 20px;
  display: grid; grid-template-columns: 1fr 350px; gap: 40px; align-items: flex-start; }
.main-content-column, .sidebar-column { min-width: 0; }

.breadcrumbs { font-size: 0.9rem; color: #888; margin-bottom: 18px; display: flex; align-items: center; gap: 8px; }
.breadcrumbs a { color: #888; text-decoration: none; }
.breadcrumbs a:hover { text-decoration: underline; }
.breadcrumbs span:last-child { font-weight: 500; color: #333; }

/* Gallery */
.gallery { margin-bottom: 22px; }
.hero-image { position: relative; width: 100%; height: clamp(208px, 38.4vw, 360px);
  border-radius: 16px; overflow: hidden; outline: none; }
.hero-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.nav { position: absolute; top: 50%; transform: translateY(-50%); z-index: 2;
  width: 42px; height: 42px; border-radius: 50%; border: none;
  background: rgba(0,0,0,0.45); color: #fff; font-size: 24px; cursor: pointer; }
.prev { left: 12px; } .next { right: 12px; } .nav:hover { background: rgba(0,0,0,0.6); }

.thumbs { margin-top: 12px; display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; scrollbar-width: thin; }
.thumb { flex: 0 0 auto; width: 90px; height: 68px; border-radius: 8px; overflow: hidden; padding: 0;
  border: 2px solid transparent; background: #fff; cursor: pointer; }
.thumb img { width: 100%; height: 100%; object-fit: cover; filter: grayscale(30%); opacity: .9; transition: .2s; }
.thumb:hover img { filter: none; opacity: 1; transform: scale(1.02); }
.thumb.active { border-color: #0A2A66; }
.thumb.active img { filter: none; opacity: 1; }

.info-header { margin: 6px 0 22px; }
.tags { margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.tags span { display: inline-block; background-color: #f0f0f0; color: #888; font-size: 0.9rem; padding: 6px 12px; border-radius: 20px; }
h1 { font-size: 2.4rem; font-weight: 800; margin: 0 0 8px; color: #222; line-height: 1.2; }
.location { font-size: 1.05rem; color: #555; font-weight: 500; }

.info-card { background: #fff; border: 1px solid #E5E5E5; border-radius: 12px; padding: 16px; margin-bottom: 24px; }
.tabs { display: flex; gap: 6px; margin-bottom: 12px; flex-wrap: wrap; }
.tabs button { border: 1px solid #d6d6d6; background: #f9f9f9; color: #333;
  padding: 8px 14px; border-radius: 999px; cursor: pointer; font-weight: 600; }
.tabs button.active { background: #0A2A66; border-color: #0A2A66; color: #fff; }

.info-table { width: 100%; border-collapse: collapse; }
.info-table th, .info-table td { border-bottom: 1px solid #eee; padding: 10px 8px; text-align: left; }
.info-table th { width: 28%; color: #6b7280; font-weight: 600; background: #fafafa; }

.detail-wrap { position: relative; }
.detail-text { line-height: 1.7; white-space: pre-line; color: #444; transition: max-height .25s ease; }
.detail-text.collapsed { max-height: 7.2em; overflow: hidden; }
.fade {
  position: absolute; left: 0; right: 0; bottom: 42px; height: 48px;
  background: linear-gradient(180deg, rgba(255,255,255,0) 0%, #fff 70%);
  pointer-events: none;
}
.more-wrap { display: flex; justify-content: flex-end; margin-top: 8px; }
.btn-more {
  background: #0A2A66; color: #fff; border: none; border-radius: 18px;
  padding: 6px 14px; font-weight: 700; cursor: pointer;
}
.btn-more:hover { filter: brightness(0.96); }

.loading-container { display: flex; justify-content: center; align-items: center; height: 50vh; }

@media (max-width: 992px) {
  .content-wrapper { grid-template-columns: 1fr; gap: 28px; }
  .sticky-sidebar { position: static; }
}
.sticky-sidebar { position: sticky; top: 88px; }

/* 근처 숙소 카드 */
.nearby-hotel-card {
  position: relative;
  padding: 24px 22px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(18, 28, 45, 0.08);
  border: 1px solid rgba(17, 24, 39, 0.06);
  overflow: hidden;
  transition: transform .18s ease, box-shadow .18s ease;
}
.nearby-hotel-card::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 6px;
  background: linear-gradient(90deg, #3b82f6, #22c55e, #06b6d4);
  opacity: .9;
}
.nearby-hotel-card:hover { transform: translateY(-3px); box-shadow: 0 12px 28px rgba(18, 28, 45, 0.12); }
.nearby-hotel-card h3 { margin: 8px 0 10px; font-size: 18px; font-weight: 800; color: #111827; letter-spacing: -0.2px; }
.nearby-hotel-card p { margin: 0 0 18px; font-size: 14.5px; line-height: 1.55; color: #4b5563; }

.btn-find-hotels {
  display: inline-flex; align-items: center; gap: 10px; padding: 11px 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb); color: #fff; font-weight: 700;
  border-radius: 10px; text-decoration: none; box-shadow: 0 6px 14px rgba(37, 99, 235, 0.25);
  transition: transform .15s ease, box-shadow .15s ease, background .2s ease;
}
.btn-find-hotels:hover { transform: translateY(-1px); background: linear-gradient(135deg, #2563eb, #1d4ed8); box-shadow: 0 10px 18px rgba(29, 78, 216, 0.32); }
.btn-find-hotels::before { content: "🔎"; font-size: 16px; line-height: 1; }

.bottom-sidebar { margin-top: 28px; }
</style>
