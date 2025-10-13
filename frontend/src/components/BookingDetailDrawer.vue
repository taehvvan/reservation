<template>
    <transition name="drawer">
      <aside v-if="open" class="drawer" aria-modal="true" role="dialog">
        <header class="drawer-header">
          <div class="title-wrap">
            <span class="status" :class="statusClass">{{ statusLabel }}</span>
            <h2>{{ booking.hotel?.name }}</h2>
          </div>
          <button class="btn-close" @click="$emit('close')" aria-label="닫기">×</button>
        </header>
  
        <section class="topline">
          <div class="id-copy">
            <strong>예약번호</strong>
            <button class="order-id-btn" @click="copyId" title="클릭하여 복사">{{ booking.bookingId }}</button>
            <button class="icon-copy-btn" @click="copyId" aria-label="복사">
              <svg class="copy-icon" viewBox="0 0 24 24" width="18" height="18"><path d="M16 1H4a2 2 0 0 0-2 2v12h2V3h12V1zm3 4H8a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2zm0 16H8V7h11v14z"/></svg>
            </button>
            <span class="copy-badge" v-show="copied" aria-live="polite">복사됨</span>
          </div>
  
          <div class="summary">
            <img :src="booking.hotel?.thumbnail" alt="" class="thumb" />
            <ul>
              <li><strong>객실</strong> {{ booking.room?.name }}</li>
              <li><strong>일정</strong> {{ dateRange }} · {{ booking.nights }}박</li>
              <li><strong>인원</strong> 성인 {{ booking.room?.occupancy?.adults }}명<span v-if="booking.room?.occupancy?.children">, 어린이 {{ booking.room?.occupancy?.children }}명</span></li>
            </ul>
          </div>
        </section>
  
        <section class="grid">
          <div class="col">
            <h3>이용 안내</h3>
            <ul class="bullets">
              <li>체크인: {{ booking.policies?.checkin || '15:00 이후' }}</li>
              <li>체크아웃: {{ booking.policies?.checkout || '11:00 이전' }}</li>
              <li v-if="booking.guest"><strong>투숙객</strong>: {{ booking.guest.name }} / {{ booking.guest.phoneMasked }}</li>
              <li v-if="booking.requests"><strong>요청사항</strong>: {{ booking.requests }}</li>
            </ul>
  
            <h3>취소/환불</h3>
            <div class="refund">
              <p v-if="refundRate !== null && pay.total != null">
                지금 취소 시 환불액: <strong>{{ formatMoney(pay.total * refundRate) }}</strong>
                <small>({{ Math.round(refundRate*100) }}%)</small>
              </p>
              <table class="policy-table" v-if="booking.policies?.cancellation?.length">
                <thead><tr><th>기한</th><th>환불율</th></tr></thead>
                <tbody>
                  <tr v-for="(r,i) in booking.policies.cancellation" :key="i">
                    <td>{{ r.until ? formatDateTime(r.until) : '체크인 이후' }}</td>
                    <td>{{ Math.round(r.refundRate*100) }}%</td>
                  </tr>
                </tbody>
              </table>
            </div>
  
            <h3>위치</h3>
            <div class="addr">
              <div class="addr-line">
                <span>{{ booking.hotel?.address }}</span>
                <button class="mini-btn" @click="copyText(booking.hotel?.address)">복사</button>
              </div>
              <div id="kakao-mini-map" class="mini-map" />
              <div class="map-actions">
                <a :href="kakaoMapLink" target="_blank" rel="noopener" class="mini-btn">지도 보기</a>
                <a :href="`tel:${booking.hotel?.phone}`" v-if="booking.hotel?.phone" class="mini-btn">전화</a>
              </div>
            </div>
          </div>
  
          <div class="col summary-col">
            <h3>결제 내역</h3>
            <ul class="price">
              <!-- ✅ 객실요금은 총 결제액을 그대로 사용 -->
              <li><span>객실요금</span><b>{{ formatMoney(pay.total) }}</b></li>
              <li><span>세금/수수료</span><b>5000원</b></li>
              <li v-if="pay.breakdown.coupon != null"><span>쿠폰</span><b>{{ formatMoney(pay.breakdown.coupon) }}</b></li>
              <li v-if="pay.breakdown.points != null"><span>포인트</span><b>{{ formatMoney(pay.breakdown.points) }}</b></li>
              <li class="total">
            <span>총 결제액</span>
            <b>{{ formatMoney((pay.total || 0) + 5000) }}</b>
            </li>
            </ul>
  
  
            <div class="actions">
              <button class="btn primary" @click="download(booking.voucherUrl)" v-if="booking.voucherUrl">바우처(PDF)</button>
              <button class="btn outline" @click="download(booking.invoiceUrl)" v-if="booking.invoiceUrl">영수증(PDF)</button>
              <button class="btn danger" :disabled="!canCancel || canceling" @click="onCancelClick">예약 취소</button>
            </div>
          </div>
        </section>
      </aside>
    </transition>
  </template>
  
  <script setup>
  import { computed, watch, ref } from 'vue'
  
  const props = defineProps({
    open: { type: Boolean, default: false },
    booking: { type: Object, default: () => ({}) }
  })
  const emit = defineEmits(['update:open','close','cancel'])
  
  const open = computed({
    get: () => props.open,
    set: v => emit('update:open', v)
  })
  
  const copied = ref(false)
  let timer = null
  
  /* 결제 데이터 안전 정규화 */
  const pay = computed(() => {
    const b = props.booking || {}
    const p = b.payment || {}
    const bd = p.breakdown || {}
    const numOrNull = (v) => (v === null || v === undefined || v === '' || Number.isNaN(Number(v))) ? null : Number(v)
    return {
      status: p.status ?? null,
      paidAt: p.paidAt ?? null,
      cardMasked: p.cardMasked ?? null,
      total: numOrNull(p.total ?? b.total ?? b.price ?? null),
      breakdown: {
        taxesAndFees: numOrNull(bd.taxesAndFees) ?? 0,
        coupon: (bd.coupon ?? null),
        points: (bd.points ?? null)
      }
    }
  })
  
  const statusLabel = computed(() => {
    const s = props.booking?.status || 'CONFIRMED'
    return s === 'CONFIRMED' ? '예약 완료'
         : s === 'PENDING'   ? '결제 대기'
         : s === 'CANCELED'  ? '취소됨'
         : s === 'REFUNDED'  ? '환불 완료'
         : s === 'NO_SHOW'   ? '노쇼'
         : s
  })
  const statusClass = computed(() => ({
    confirmed: props.booking?.status === 'CONFIRMED',
    pending:   props.booking?.status === 'PENDING',
    canceled:  props.booking?.status === 'CANCELED',
    refunded:  props.booking?.status === 'REFUNDED',
    noshow:    props.booking?.status === 'NO_SHOW'
  }))
  const dateRange = computed(() => `${formatDate(props.booking?.checkIn)} ~ ${formatDate(props.booking?.checkOut)}`)
  const kakaoMapLink = computed(() => {
    const lat = props.booking?.hotel?.lat
    const lng = props.booking?.hotel?.lng
    return lat && lng
      ? `https://map.kakao.com/link/map/${encodeURIComponent(props.booking.hotel.name)},${lat},${lng}`
      : `https://map.kakao.com/?q=${encodeURIComponent(props.booking?.hotel?.address||'')}`
  })
  const canCancel = computed(() => props.booking?.status === 'CONFIRMED')
  
  /* 포맷터 */
  function formatMoney(v){
    if (v === null || v === undefined || v === '' || Number.isNaN(Number(v))) return '-'
    return Number(v).toLocaleString('ko-KR') + '원'
  }
  function pad(n){ return String(n).padStart(2,'0') }
  function toDateSafe(v){
    if (v === null || v === undefined || v === '') return null
    if (typeof v === 'number') return new Date(v)
    const s = String(v).replace(' ', 'T')
    const d = new Date(s)
    return isNaN(d.getTime()) ? null : d
  }
  function formatDate(iso){
    const d = toDateSafe(iso); if(!d) return '-'
    const w = ['일','월','화','수','목','금','토'][d.getDay()]
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}(${w})`
  }
  function formatDateTime(v){
    const d = toDateSafe(v); if(!d) return '-'
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  }
  
  async function copyId(){ await copyText(props.booking?.bookingId) }
  async function copyText(t){
    if(!t) return
    try{
      if(navigator.clipboard && window.isSecureContext){
        await navigator.clipboard.writeText(t)
      }else{
        const ta = document.createElement('textarea')
        ta.value = t; ta.setAttribute('readonly',''); ta.style.position='fixed'; ta.style.left='-9999px'
        document.body.appendChild(ta); ta.select(); document.execCommand('copy'); document.body.removeChild(ta)
      }
      copied.value = true; clearTimeout(timer); timer = setTimeout(()=>copied.value=false, 1500)
    }catch(e){ console.error(e) }
  }
  
  const refundRate = computed(()=>{
    const rules = props.booking?.policies?.cancellation || []
    if(!rules.length) return null
    const now = new Date()
    const matched = rules.find(r => r.until && now <= new Date(r.until))
    if(matched) return matched.refundRate
    const last = rules[rules.length-1]
    return last ? last.refundRate : null
  })
  
  watch(()=>props.open, (v)=>{ if(v) initKakaoMiniMap() })
  
  function initKakaoMiniMap(){
    const lat = props.booking?.hotel?.lat
    const lng = props.booking?.hotel?.lng
    if(!lat || !lng) return
  
    const load = () =>
      window.kakao && window.kakao.maps
        ? Promise.resolve()
        : new Promise((res)=>{
            const s = document.createElement('script')
            s.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${import.meta.env.VITE_KAKAO_JS_KEY}&autoload=false`
            s.onload = () => window.kakao.maps.load(res)
            document.head.appendChild(s)
          })
  
    load().then(()=>{
      const container = document.getElementById('kakao-mini-map')
      if(!container) return
      const center = new window.kakao.maps.LatLng(lat, lng)
      const map = new window.kakao.maps.Map(container, { center, level: 4 })
      new window.kakao.maps.Marker({ position: center, map })
    })
  }
  
  const canceling = ref(false)
  function onCancelClick(){
    if(canceling.value) return
    canceling.value = true
    emit('cancel', props.booking.bookingId) // 부모가 성공 시 닫음
    setTimeout(()=>{ canceling.value = false }, 1500) // 실패 대비 복구
  }
  
  function download(url){ if(url) window.open(url, '_blank') }
  </script>
  
  <style scoped>
  .drawer-enter-from, .drawer-leave-to { transform: translateX(100%); opacity: 0; }
  .drawer-enter-active, .drawer-leave-active { transition: all .2s ease; }
  .drawer { position: fixed; top:0; right:0; width:820px; max-width:90vw; height:100vh; background:#fff; box-shadow:-12px 0 30px rgba(0,0,0,.15); z-index:1000; overflow:auto; }
  
  .drawer-header{ display:flex; align-items:center; justify-content:space-between; padding:18px 22px; border-bottom:1px solid #eee; position:sticky; top:0; background:#fff; z-index:1;}
  .title-wrap h2{ margin:6px 0 0; font-size:20px; }
  .status{ padding:4px 10px; border-radius:999px; font-size:12px; margin-right:8px; }
  .status.confirmed{ background:#e8f1ff; color:#0A2A66; }
  .status.pending{ background:#fff4e5; color:#a15c00; }
  .status.canceled{ background:#f4f4f5; color:#6b7280; }
  .status.refunded{ background:#eaffee; color:#1a7f37; }
  .status.noshow{ background:#fde8e8; color:#b91c1c; }
  
  .btn-close{ font-size:24px; width:36px; height:36px; line-height:34px; border:none; background:#f5f5f5; border-radius:8px; cursor:pointer; }
  .topline{ padding:18px 22px; border-bottom:1px solid #f0f0f0; }
  .id-copy{ display:flex; align-items:center; gap:6px; margin-bottom:12px; }
  .order-id-btn{ background:#eef4ff; border:1px dashed #0A2A66; padding:4px 8px; border-radius:8px; cursor:pointer; font-family:ui-monospace,Menlo,Consolas,monospace; }
  .icon-copy-btn{ width:28px; height:28px; display:inline-flex; align-items:center; justify-content:center; border:1px solid #cdd7ee; background:#fff; border-radius:6px; cursor:pointer; }
  .copy-icon{ fill:#0A2A66; }
  .copy-badge{ background:#eaffee; border:1px solid #b7efc2; color:#1a7f37; padding:2px 8px; border-radius:999px; font-size:12px; }
  
  .summary{ display:flex; gap:14px; align-items:center; }
  .summary .thumb{ width:96px; height:72px; object-fit:cover; border-radius:8px; }
  .summary ul{ list-style:none; padding:0; margin:0; }
  .summary li{ margin:2px 0; }
  
  .grid{ display:grid; grid-template-columns:1fr 360px; gap:22px; padding:18px 22px 30px; }
  .col h3{ margin:18px 0 8px; }
  .bullets{ padding-left:16px; }
  .bullets li{ margin:4px 0; }
  
  .addr .addr-line{ display:flex; gap:8px; align-items:center; }
  .mini-map{ width:100%; height:220px; border-radius:10px; border:1px solid #eee; margin-top:8px; }
  .map-actions{ display:flex; gap:8px; margin-top:8px; }
  .mini-btn{ padding:6px 10px; font-size:12px; border:1px solid #e5e7eb; background:#fff; border-radius:6px; cursor:pointer; text-decoration:none; }
  
  .summary-col{ position:sticky; top:58px; height: fit-content; align-self:start; }
  .price{ list-style:none; padding:0; margin:0 0 10px; }
  .price li{ display:flex; align-items:center; justify-content:space-between; padding:6px 0; }
  .price li.total{ border-top:1px dashed #eee; margin-top:6px; padding-top:10px; font-size:18px; }
  .pay-meta p{ margin:4px 0; font-size:14px; color:#555; }
  
  .actions{ display:flex; flex-direction:column; gap:10px; margin-top:14px; }
  .btn{ padding:12px; border-radius:10px; font-weight:700; cursor:pointer; border:1px solid transparent; }
  .btn.primary{ background:#0A2A66; color:#fff; }
  .btn.outline{ background:#fff; color:#0A2A66; border-color:#0A2A66; }
  .btn.danger{ background:#fff5f5; color:#b91c1c; border-color:#fecaca; }
  .mini-map { display: none; }
  </style>
  