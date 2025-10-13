<template>
  <div class="mypage-container">
    <div class="content-wrapper">
      <div class="user-profile">
        <div class="profile-info">
          <h3>{{ userInfo.name }}님</h3>
          <p>{{ userInfo.email }}</p>
          <div class="profile-actions">
            <button @click="changeTab('profile')">회원정보 수정</button>
            <button @click="logout">로그아웃</button>
          </div>
        </div>
      </div>

      <div class="mypage-content">
        <div class="mypage-menu">
          <button :class="{ active: activeTab === 'reservations' }" @click="changeTab('reservations')"><span>예약 내역</span></button>
          <button :class="{ active: activeTab === 'reviews' }" @click="changeTab('reviews')"><span>내가 쓴 후기</span></button>
          <button :class="{ active: activeTab === 'inquiries' }" @click="changeTab('inquiries')"><span>내 문의내역</span></button>
          <button :class="{ active: activeTab === 'coupons' }" @click="changeTab('coupons')"><span>쿠폰함</span></button>
        </div>

        <div class="tab-content">
          <div v-if="activeTab === 'reservations'" class="tab-pane">
            <div class="section-header">
              <h4>예약 내역</h4>
              <span class="header-line"></span>
            </div>

            <div v-if="reservations && reservations.length > 0">
              <div v-if="completedReservations.length > 0" class="reservation-list">
                <h3>예약 완료</h3>
                <div
                  v-for="reservation in completedReservations"
                  :key="reservation.reservationId"
                  class="reservation-card clickable"
                  @click="openReservationDetail(reservation)"
                  role="button"
                  tabindex="0"
                >
                  <div class="card-image">
                    <img :src="reservation.image" :alt="reservation.roomType || reservation.hotelName" />
                  </div>
                  <div class="card-info">
                    <h5 class="place-name">{{ reservation.placeName }}</h5>
                    <p class="order-id"><strong>예약 번호:</strong> {{ reservation.orderId }}</p>
                    <p class="reservation-details">
                      <span><strong>객실:</strong> {{ reservation.roomType }}</span>
                      <span><strong>주소:</strong> {{ reservation.address }}</span>
                    </p>
                    <div class="reservation-actions">
                      <p class="dates">{{ reservation.checkIn }} ~ {{ reservation.checkOut }}</p>
                      <p class="price-people">
                        <span>가격: {{ reservation.price.toLocaleString() }}원</span>
                        <span>인원: {{ reservation.guests }}명</span>
                      </p>

                      <button
                        v-if="isReviewable(reservation)"
                        @click.stop="openReviewModal(reservation)"
                        class="btn-review-write"
                      >후기 작성</button>

                      <p v-else-if="reservation.status === '리뷰 작성 완료'" class="status-badge reviewed">작성 완료</p>

                      <button
                        v-if="reservation.status === '리뷰 작성 완료'"
                        @click.stop="deleteCompletedReservation(reservation.reservationId)"
                        class="btn-delete-completed"
                      >예약 내역 삭제</button>

                      <button
                        v-if="reservation.status === '예약 완료' && !isReviewable(reservation)"
                        @click.stop="cancelReservation(reservation.reservationId)"
                        class="btn-cancel-reservation"
                      >예약 취소</button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="cancelledReservations.length > 0" class="reservation-list" style="margin-top: 20px;">
                <h3>취소 내역</h3>
                <div
                  v-for="reservation in cancelledReservations"
                  :key="reservation.reservationId"
                  class="reservation-card disabled clickable"
                  @click="openReservationDetail(reservation)"
                  role="button"
                  tabindex="0"
                >
                  <div class="card-image">
                    <img :src="reservation.image" :alt="reservation.roomType || reservation.hotelName" />
                  </div>
                  <div class="card-info">
                    <h5 class="place-name">{{ reservation.placeName }}</h5>
                    <p class="reservation-details">
                      <span><strong>객실:</strong> {{ reservation.roomType }}</span>
                      <span><strong>주소:</strong> {{ reservation.address }}</span>
                    </p>
                    <div class="reservation-actions">
                      <p class="status-badge cancelled">예약 취소</p>
                      <p class="dates">{{ reservation.checkIn }} ~ {{ reservation.checkOut }}</p>
                      <p class="price-people">
                        <span>가격: {{ reservation.price.toLocaleString() }}원</span>
                        <span>인원: {{ reservation.guests }}명</span>
                      </p>
                      <button
                        @click.stop="deleteCancelledReservation(reservation.reservationId)"
                        class="btn-delete-cancelled"
                      >취소 내역 삭제</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <p>아직 예약 내역이 없습니다. 새로운 쉼을 찾아 떠나보세요! ✨</p>
            </div>
          </div>

          <div v-else-if="activeTab === 'reviews'" class="tab-pane">
            <div class="section-header">
              <h4>내가 쓴 후기</h4>
              <span class="header-line"></span>
            </div>
            <div v-if="myReviews.length > 0" class="review-list">
              <div v-for="review in myReviews" :key="review.reviewId" class="review-card">
                <div class="review-card-header">
                  <span class="review-hotel-name">{{ review.hotelName }}</span>
                  <span class="review-user-name">작성자: {{ review.userName }}</span>
                  <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                </div>
                <div class="review-rating">
                  <span v-for="i in review.score" :key="i" class="star">★</span>
                </div>
                <p class="review-content">{{ review.content }}</p>
              </div>
            </div>
            <div v-else class="empty-state">
              <p>아직 작성한 후기가 없습니다. 📝</p>
            </div>
          </div>

          <div v-else-if="activeTab === 'inquiries'" class="tab-pane">
            <div class="section-header">
              <h4>내 문의내역</h4>
              <span class="header-line"></span>
            </div>
            <div v-if="myInquiries.length > 0" class="inquiry-list">
              <div v-for="inquiry in myInquiries" :key="inquiry.id" class="inquiry-card" @click="openInquiryModal(inquiry)">
                <div class="inquiry-title">
                  <span class="inquiry-status" :class="inquiry.status.toLowerCase()">
                    {{ inquiry.status === 'ANSWERED' ? '답변완료' : '대기중' }}
                  </span>
                  <p>{{ inquiry.title }}</p>
                </div>
                <div class="inquiry-date">{{ formatDate(inquiry.createdAt) }}</div>
              </div>
            </div>
            <div v-else class="empty-state">
              <p>아직 작성한 문의가 없습니다. 🤔</p>
            </div>
          </div>

          <div v-else-if="activeTab === 'coupons'" class="tab-pane">
            <div class="section-header">
              <h4>쿠폰함</h4>
              <span class="header-line"></span>
            </div>
            <div v-if="coupons.length > 0" class="coupon-list">
              <div v-for="coupon in coupons" :key="coupon.userCouponId" class="coupon-card" :class="{ expired: coupon.isExpired }">
                <div class="coupon-discount"><strong>{{ formatDiscount(coupon) }}</strong></div>
                <div class="coupon-info">
                  <h4>{{ coupon.name }}</h4>
                  <p>유효기간: {{ coupon.expiryDate }} 까지</p>
                </div>
                <div class="coupon-actions">
                  <span v-if="coupon.isExpired" class="expired-badge">기간 만료</span>
                  <button v-if="coupon.isExpired" @click="deleteCoupon(coupon.userCouponId)" class="btn-delete-coupon">삭제</button>
                </div>
              </div>
            </div>
            <div v-else class="empty-state"><p>현재 사용 가능한 쿠폰이 없습니다. 💸</p></div>
          </div>

          <div v-else-if="activeTab === 'profile'" class="tab-pane">
            <div class="section-header">
              <h4>회원정보 수정</h4>
              <span class="header-line"></span>
            </div>
            <form @submit.prevent class="profile-form">
              <div class="form-group">
                <label for="name">이름</label>
                <div class="input-with-button"><input type="text" id="name" v-model="editInfo.name" required /></div>
              </div>
              <div class="form-group">
                <label for="phone">전화번호</label>
                <div class="input-with-button">
                  <input type="tel" id="phone" v-model="editInfo.phone" placeholder="'-' 없이 입력" />
                  <button type="button" class="btn-update-field" @click="updatePhoneNumber">전화번호 저장</button>
                </div>
              </div>

              <div class="password-change-section">
                <h5 class="password-section-title">비밀번호 변경</h5>

                <div class="form-group" v-if="!isPasswordVerified">
                  <label for="old-password">기존 비밀번호</label>
                  <div class="input-with-button">
                    <input type="password" id="old-password" v-model="oldPassword" class="form-input" placeholder="기존 비밀번호 입력" />
                    <button type="button" class="btn-update-field" @click="handleOldPasswordCheck" :disabled="!oldPassword">인증 메일 발송</button>
                  </div>
                </div>

                <div class="form-group" v-if="isPasswordVerified && !isCodeVerified">
                  <label for="verificationCode">이메일 인증코드</label>
                  <div class="input-with-button">
                    <input type="text" id="verificationCode" v-model="verificationCode" placeholder="이메일로 발송된 인증코드 입력" />
                    <button type="button" class="btn-update-field" @click="handleVerifyCode" :disabled="!verificationCode || verificationCode.length < 6">인증 확인</button>
                  </div>
                </div>

                <div v-if="isCodeVerified">
                  <div class="form-group">
                    <label for="new-password">새 비밀번호</label>
                    <input type="password" id="new-password" v-model="newPassword" placeholder="8자 이상, 영문, 숫자, 특수문자 포함" />
                  </div>
                  <div class="form-group">
                    <label for="confirm-password">새 비밀번호 확인</label>
                    <input type="password" id="confirm-password" v-model="confirmPassword" placeholder="새 비밀번호 다시 입력" />
                  </div>
                  <div class="input-with-button">
                    <button type="button" class="btn-update-field" @click="handleResetPassword" :disabled="!isFormValid">비밀번호 변경</button>
                  </div>
                </div>

                <p v-if="statusMessage" class="status-message">{{ statusMessage }}</p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <BookingDetailDrawer
      v-model:open="drawerOpen"
      :booking="selectedBooking"
      @close="drawerOpen = false"
      @cancel="handleCancelFromDrawer"
    />

    <div v-if="isReviewModalOpen" class="modal-overlay" @click.self="closeReviewModal">
      <div class="modal-content">
        <h4 class="modal-title">후기 작성</h4>
        <div class="modal-hotel-info">
          <strong>{{ selectedReservationForReview.placeName }}</strong>
          <p>{{ selectedReservationForReview.roomType }}</p>
        </div>
        <div class="review-form">
          <div class="form-group">
            <label>별점</label>
            <div class="star-rating">
              <span v-for="star in 5" :key="star" @click="reviewData.score = star" :class="{ 'filled': star <= reviewData.score }">★</span>
            </div>
          </div>
          <div class="form-group">
            <label>후기 내용</label>
            <textarea v-model="reviewData.content" rows="5" placeholder="숙소에서의 경험을 공유해주세요."></textarea>
          </div>
          <div class="modal-actions">
            <button @click="closeReviewModal" class="btn-cancel">취소</button>
            <button @click="submitReview" class="btn-submit">등록하기</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="isInquiryModalOpen" class="modal-overlay" @click.self="closeInquiryModal">
      <div class="modal-content inquiry-modal-content">
        <h4 class="modal-title">문의 상세</h4>
        <div v-if="selectedInquiry">
          <div class="inquiry-detail-section">
            <div class="inquiry-detail-header">
              <span class="inquiry-status" :class="selectedInquiry.status.toLowerCase()">
                {{ selectedInquiry.status === 'ANSWERED' ? '답변완료' : '대기중' }}
              </span>
              <strong class="inquiry-detail-title">{{ selectedInquiry.title }}</strong>
            </div>
             <p class="inquiry-detail-date">작성일: {{ new Date(selectedInquiry.createdAt).toLocaleString() }}</p>
          </div>
          
          <div class="inquiry-detail-content">
            <h5>문의 내용</h5>
            <p class="content-box">{{ selectedInquiry.content }}</p>
          </div>

          <div v-if="selectedInquiry.status === 'ANSWERED'" class="inquiry-detail-content answer">
            <h5>답변 내용</h5>
            <div class="content-box answer-box">
              <p>{{ selectedInquiry.answer }}</p>
              <p class="answer-date">답변일: {{ new Date(selectedInquiry.answeredAt).toLocaleString() }}</p>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="closeInquiryModal" class="btn-submit">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue' // watch 추가
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/booking'
import { useAuthStore } from '@/stores/auth'
import BookingDetailDrawer from '@/components/BookingDetailDrawer.vue'

const activeTab = ref('reservations')
const bookingStore = useBookingStore()
const authStore = useAuthStore()
const hotel = computed(() => bookingStore.hotel)

const userInfo = reactive({ name: '', email: '' })
const editInfo = reactive({ name: '', phone: '' })
const reservations = ref([])
const coupons = ref([])
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const verificationCode = ref('')
const passwordResetToken = ref('')
const isPasswordVerified = ref(false)
const isCodeVerified = ref(false)
const statusMessage = ref('')

const myInquiries = ref([])
const isInquiryModalOpen = ref(false)
const selectedInquiry = ref(null)

const route = useRoute()
const router = useRouter()

const drawerOpen = ref(false)
const selectedBooking = ref(null)

const completedReservations = computed(() =>
  (reservations.value || []).filter(r => r.status === '예약 완료' || r.status === '리뷰 작성 완료')
)
const cancelledReservations = computed(() =>
  (reservations.value || []).filter(r => r.status === '예약 취소')
)

const myReviews = ref([])
const isReviewModalOpen = ref(false)
const selectedReservationForReview = ref(null)
const reviewData = reactive({ reservationId: null, score: 0, content: '' })

onMounted(() => {
  // URL 쿼리 파라미터를 확인하여 초기 탭 설정
  const initialTab = route.query.tab || 'reservations';
  changeTab(initialTab);

  fetchUserData()
  fetchReservations()
  fetchMyReviews()
  fetchMyInquiries()
})

// [추가] 라우트 쿼리가 변경될 때 탭을 동기화
watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab;
  }
});


function openReservationDetail(reservation) {
  selectedBooking.value = mapReservationToBooking(reservation)
  drawerOpen.value = true
}

async function handleCancelFromDrawer() {
  const id = selectedBooking.value?.__raw?.reservationId
  if (!id) return
  const ok = await cancelReservation(id)
  if (ok) {
    drawerOpen.value = false
    selectedBooking.value = null
  }
}

function mapReservationToBooking(r) {
  const nights = diffNights(r.checkIn, r.checkOut)
  return {
    bookingId: r.orderId || r.reservationId,
    status: toEnumStatus(r.status),
    createdAt: r.createdAt ?? r.reservationCreatedAt ?? null,
    hotel: {
      id: r.hotelId,
      name: r.placeName || r.hotelName,
      address: r.address,
      thumbnail: r.image
    },
    room: { name: r.roomType, occupancy: { adults: r.guests || 0, children: r.children || 0 } },
    checkIn: toISO(r.checkIn),
    checkOut: toISO(r.checkOut),
    nights,
    payment: {
      total: r.total ?? r.price ?? null,
      paidAt: r.paidAt ?? null,
      status: r.paymentStatus ?? null,
      breakdown: {
        taxesAndFees: r.taxesAndFees ?? 0,
        coupon: r.coupon ?? null,
        points: r.points ?? null
      }
    },
    policies: r.policies || { checkin: r.checkinTime, checkout: r.checkoutTime, cancellation: r.cancellationRules },
    __raw: r
  }
}
function diffNights(a, b) { const d1 = new Date(a), d2 = new Date(b); return Math.max(1, Math.round((d2-d1)/(24*60*60*1000))) }
function toISO(s){ if(!s) return null; if(String(s).includes('T')) return s; return new Date(s + 'T00:00:00').toISOString() }
function toEnumStatus(s){
  switch (s) {
    case '예약 완료': return 'CONFIRMED'
    case '결제 대기': return 'PENDING'
    case '예약 취소':
    case '취소 완료': return 'CANCELED'
    case '환불 완료': return 'REFUNDED'
    case '노쇼': return 'NO_SHOW'
    default: return 'CONFIRMED'
  }
}

const fetchUserData = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/users/info`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    userInfo.name = response.data.name
    userInfo.email = response.data.email
    editInfo.name = response.data.name
    editInfo.phone = response.data.phone
  } catch (error) {
    console.error('사용자 정보 가져오기 실패:', error)
  }
}

const fetchReservations = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/mypage/reservations`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    reservations.value = response.data.map(item => {
      const finalImageSrc = item.hotelImage || `${import.meta.env.VITE_APP_API_URL}/images/${item.hotelType || 'default'}/${item.hotelId || '0'}.jpg`
      return {
        reservationId: item.reservationId,
        orderId: item.orderId,
        placeName: item.hotelName,
        image: finalImageSrc,
        guests: item.people || item.guestCount,
        checkIn: item.checkIn,
        checkOut: item.checkOut,
        price: item.price,
        status: item.status,
        roomType: item.roomType,
        address: item.address,
        hotelType: item.hotelType || bookingStore.hotel?.type || 'default',
        hotelId: item.hotelId || bookingStore.hotel?.hId || '0',
        createdAt: item.createdAt || item.reservationCreatedAt || null,
        taxesAndFees: item.taxesAndFees,
        coupon: item.coupon,
        points: item.points,
        total: item.total ?? item.price
      }
    })
  } catch (error) {
    console.error('예약 내역 가져오기 실패:', error)
    if (route.query.reservationId) {
      reservations.value.push({
        reservationId: route.query.reservationId,
        orderId: route.query.orderId,
        placeName: route.query.hotelName,
        image: `${import.meta.env.VITE_APP_API_URL}/images/${route.query.hotelType || 'default'}/${route.query.hotelId || '0'}.jpg`,
        guests: route.query.people,
        checkIn: route.query.checkIn,
        checkOut: route.query.checkOut,
        price: route.query.price,
        status: route.query.status,
        roomType: route.query.roomType,
        address: route.query.address,
        hotelType: route.query.hotelType || bookingStore.hotel?.type || 'default',
        hotelId: route.query.hotelId || bookingStore.hotel?.hId || '0',
        createdAt: route.query.createdAt || null
      })
    }
  }
}

const fetchMyReviews = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/reviews/my-reviews`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    myReviews.value = response.data
  } catch (error) {
    console.error('내가 쓴 후기 목록 가져오기 실패:', error)
  }
}

const fetchMyInquiries = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/inquiries/my`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    });
    myInquiries.value = response.data;
  } catch (error) {
    if (error.response?.status !== 401) {
      console.error('내 문의내역을 불러오는 데 실패했습니다.', error);
      alert('문의내역을 불러오는 데 실패했습니다.');
    } else {
      console.error('인증되지 않은 사용자의 문의내역 요청:', error);
    }
  }
};

const fetchMyCoupons = async () => {
  try {
    const token = localStorage.getItem('accessToken')
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/coupons/my-coupons`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    coupons.value = response.data
  } catch (error) {
    console.error('내 쿠폰 목록을 불러오는 데 실패했습니다:', error)
  }
}

const openReviewModal = (reservation) => {
  selectedReservationForReview.value = reservation
  reviewData.reservationId = reservation.reservationId
  reviewData.score = 0
  reviewData.content = ''
  isReviewModalOpen.value = true
}
const closeReviewModal = () => { isReviewModalOpen.value = false }

const openInquiryModal = (inquiry) => {
  selectedInquiry.value = inquiry;
  isInquiryModalOpen.value = true;
};
const closeInquiryModal = () => {
  isInquiryModalOpen.value = false;
};

const submitReview = async () => {
  if (reviewData.score === 0) return alert('별점을 선택해주세요.')
  if (!reviewData.content.trim()) return alert('후기 내용을 입력해주세요.')

  try {
    await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/reviews`, {
      reservationId: reviewData.reservationId, score: reviewData.score, content: reviewData.content
    }, { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } })
    alert('후기가 성공적으로 등록되었습니다.')
    closeReviewModal()
    fetchReservations()
    fetchMyReviews()
  } catch (error) {
    console.error('리뷰 제출 실패:', error)
    alert(error.response?.data?.message || '후기 등록에 실패했습니다.')
  }
}

const isReviewable = (reservation) => {
  const today = new Date(); today.setHours(0,0,0,0)
  const checkoutDate = new Date(reservation.checkOut)
  return reservation.status === '예약 완료' && checkoutDate < today
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}
const formatDiscount = (coupon) => {
  if (coupon.type === 'PERCENT') return `${coupon.discount}%`
  if (coupon.type === 'FIXED') return `${coupon.discount.toLocaleString()}원`
  return ''
}

const changeTab = (tabName) => {
  activeTab.value = tabName;
  // URL의 쿼리 파라미터도 변경하여 새로고침 시 탭 유지
  router.push({ path: '/mypage', query: { tab: tabName } });
  
  if (tabName === 'reviews') {
    fetchMyReviews()
  } else if (tabName === 'coupons') {
    fetchMyCoupons()
  } else if (tabName === 'inquiries') {
    fetchMyInquiries()
  } else if (tabName === 'reservations') {
    fetchReservations()
  }
}

const logout = () => {
  authStore.logout();
  router.push('/')
  alert('로그아웃 되었습니다.')
}

const cancelReservation = async (reservationId) => {
  const confirmed = confirm('예약을 취소하시겠습니까?')
  if (!confirmed) return false
  try {
    const response = await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/reservations/${reservationId}/cancel`, {}, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })

    const canceledAmount = response.data.canceledAmount
    if (canceledAmount !== undefined) {
      alert(`예약이 취소되었습니다.\n환불 예정 금액: ${canceledAmount.toLocaleString()}원`)
    } else {
      alert(response.data.message || '예약이 취소되었습니다.')
    }

    await fetchReservations()

    const res = reservations.value.find(r => r.reservationId === reservationId)
    if (res) { res.status = 'cancelled'; res.statusText = '예약 취소' }

    return true
  } catch (error) {
    console.error('예약 취소 실패:', error)
    alert(error.response?.data?.message || '예약 취소에 실패했습니다.')
    return false
  }
}

const deleteCompletedReservation = async (reservationId) => {
  const confirmed = confirm('이 예약 내역을 삭제하시겠습니까?\n작성된 리뷰는 삭제되지 않습니다.')
  if (!confirmed) return
  try {
    await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/reservations/${reservationId}/delete`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    reservations.value = reservations.value.filter(r => r.reservationId !== reservationId)
    alert('예약 내역이 삭제되었습니다.')
  } catch (error) {
    console.error('예약 내역 삭제 실패:', error)
    alert(error.response?.data?.message || '예약 내역 삭제에 실패했습니다.')
  }
}

const deleteCancelledReservation = async (reservationId) => {
  const confirmed = confirm('정말 이 취소된 예약 내역을 삭제하시겠습니까?')
  if (!confirmed) return
  try {
    await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/reservations/${reservationId}/delete`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    reservations.value = reservations.value.filter(r => r.reservationId !== reservationId)
    alert('취소 내역이 삭제되었습니다.')
  } catch (error) {
    console.error('취소 내역 삭제 실패:', error)
    alert(error.response?.data?.message || '취소 내역 삭제에 실패했습니다.')
  }
}

const updatePhoneNumber = async () => {
  if (!editInfo.phone) return alert('전화번호를 입력해주세요.')
  try {
    const response = await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/users/phone`,
      { phone: editInfo.phone },
      { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
    )
    alert(response.data.message)
    await fetchUserData()
  } catch (error) {
    console.error('전화번호 업데이트 실패:', error)
    alert(error.response?.data?.message || '전화번호 저장에 실패했습니다.')
  }
}
const handleOldPasswordCheck = async () => {
  try {
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/users/password/verify-and-send-code`,
      { oldPassword: oldPassword.value },
      { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
    )
    isPasswordVerified.value = true
    statusMessage.value = response.data.message
    alert(response.data.message)
  } catch (error) {
    console.error('기존 비밀번호 확인 실패:', error)
    statusMessage.value = error.response?.data?.message || '오류가 발생했습니다.'
    alert(statusMessage.value)
  }
}
const handleVerifyCode = async () => {
  try {
    const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/users/password/verify-code`,
      { code: verificationCode.value },
      { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
    )
    passwordResetToken.value = response.data.resetToken
    isCodeVerified.value = true
    statusMessage.value = response.data.message
    alert(response.data.message)
  } catch (error) {
    console.error('인증코드 확인 실패:', error)
    statusMessage.value = error.response?.data?.message || '인증에 실패했습니다.'
    alert(statusMessage.value)
  }
}
const handleResetPassword = async () => {
  if (!isFormValid.value) {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/
    if (newPassword.value !== confirmPassword.value) alert('새 비밀번호가 일치하지 않습니다.')
    else if (!passwordRegex.test(newPassword.value)) alert('비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.')
    return
  }
  try {
    const response = await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/users/password/reset`,
      { resetToken: passwordResetToken.value, newPassword: newPassword.value },
      { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
    )
    alert(response.data.message)
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    verificationCode.value = ''
    passwordResetToken.value = ''
    isPasswordVerified.value = false
    isCodeVerified.value = false
    statusMessage.value = ''
  } catch (error) {
    console.error('비밀번호 변경 실패:', error)
    alert(error.response?.data?.message || '비밀번호 변경에 실패했습니다.')
  }
}
const isFormValid = computed(() => {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/
  return isCodeVerified.value &&
         newPassword.value &&
         passwordRegex.test(newPassword.value) &&
         newPassword.value === confirmPassword.value
})
const deleteCoupon = async (userCouponId) => {
  if (!confirm('이 쿠폰을 삭제하시겠습니까?')) return
  try {
    await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/coupons/my-coupons/${userCouponId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    })
    alert('쿠폰이 삭제되었습니다.')
    await fetchMyCoupons()
  } catch (error) {
    console.error('쿠폰 삭제 실패:', error)
    alert('쿠폰 삭제 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@400;700;800&family=Noto+Sans+KR:wght@300;400;500;700&display=swap');

.inquiry-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.inquiry-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s, box-shadow 0.2s;
}
.inquiry-card:hover {
  background-color: #f9f9f9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.inquiry-title {
  display: flex;
  align-items: center;
  gap: 15px;
}
.inquiry-title p {
  margin: 0;
  font-weight: 500;
}
.inquiry-status {
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}
.inquiry-status.pending {
  background-color: #ff9800;
}
.inquiry-status.answered {
  background-color: #4caf50;
}
.inquiry-date {
  font-size: 0.9rem;
  color: #888;
}

.inquiry-modal-content {
  max-width: 600px;
}
.inquiry-detail-section {
  margin-bottom: 20px;
}
.inquiry-detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}
.inquiry-detail-title {
  font-size: 1.3rem;
  font-weight: 700;
}
.inquiry-detail-date {
  font-size: 0.85rem;
  color: #888;
  margin: 0;
}
.inquiry-detail-content {
  margin-bottom: 20px;
}
.inquiry-detail-content h5 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}
.content-box {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  min-height: 100px;
  white-space: pre-wrap;
  color: #333;
  line-height: 1.6;
}
.answer-box {
  background-color: #f0f4ff;
}
.answer-date {
  text-align: right;
  margin-top: 10px;
  font-size: 0.8rem;
}
.coupon-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}
.coupon-card {
    display: grid;
    grid-template-columns: 120px 1fr auto;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 5px rgba(0,0,0,0.05);
    align-items: center;
}
.coupon-card.expired {
    background-color: #f8f9fa;
    opacity: 0.7;
}
.coupon-discount {
    background-color: #4A69A1;
    color: white;
    padding: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.8rem;
    font-weight: 800;
    text-align: center;
    height: 100%;
}
.coupon-card.expired .coupon-discount {
    background-color: #adb5bd;
}
.coupon-info {
    padding: 15px;
}
.coupon-info h4 {
    margin: 0 0 5px 0;
    font-size: 1.1rem;
}
.coupon-info p {
    margin: 0;
    font-size: 0.9rem;
    color: #777;
}
.coupon-actions {
    padding: 15px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}
.expired-badge {
    font-size: 0.9rem;
    font-weight: 700;
    color: #e63946;
}
.btn-delete-coupon {
    background-color: #6c757d;
    color: white;
    border: none;
    padding: 6px 12px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.85rem;
}

.btn-delete-completed {
  background-color: #888;
  color: #fff;
  border: none;
  padding: 8px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  margin-top: 8px;
}

.btn-delete-completed:hover {
  background-color: #555;
}

.review-user-name {
    font-size: 0.9rem;
    color: #888;
    margin-left: 10px;
}

.status-badge.reviewed {
    background-color: #E0E0E0;
    color: #757575;
    font-weight: 500;
    margin-top: 8px;
    padding: 8px 15px;
}

.order-id {
  font-size: 0.9rem;
  color: #666;
  margin: 5px 0 10px 0;
}

.mypage-container {
  min-height: 100vh;
  background-color: #F8F4EF;
  padding: 60px 0;
  font-family: 'Noto Sans KR', sans-serif;
  color: #333;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 40px;
  padding: 0 20px;
}

.user-profile {
  position: sticky;
  top: 40px;
  height: fit-content;
  background-color: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  border: 1px solid #E0E0E0;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.profile-info {
  margin-top: 20px;
}
.profile-info h3 {
  font-family: 'Nanum Myeongjo', serif;
  font-weight: 700;
  font-size: 1.5rem;
  margin-bottom: 5px;
}
.profile-info p {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
}
.profile-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}
.profile-actions button {
  background: none;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 8px 15px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}
.profile-actions button:hover {
  background-color: #EFEFEF;
}
.profile-actions button:last-child {
  background-color: #333;
  color: #fff;
  border-color: #333;
}
.profile-actions button:last-child:hover {
  background-color: #000;
}

.mypage-content {
  background-color: #fff;
  border: 1px solid #E0E0E0;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.mypage-menu {
  display: flex;
  justify-content: space-around;
  margin-bottom: 40px;
  border-bottom: 2px solid #EEE;
  gap: 10px;
}
.mypage-menu button {
  flex: 1;
  padding: 15px 20px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 1.1rem;
  font-weight: 600;
  color: #888;
  cursor: pointer;
  transition: all 0.3s;
}
.mypage-menu button.active {
  color: #333;
  border-bottom-color: #333;
}
.mypage-menu button:hover {
  color: #333;
}

.section-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 30px;
}
.section-header h4 {
  font-family: 'Nanum Myeongjo', serif;
  font-weight: 800;
  font-size: 1.6rem;
  margin: 0;
}
.section-header .header-line {
  width: 100%;
  height: 2px;
  background: #333;
  margin-top: 5px;
  position: relative;
}
.section-header .header-line::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: #E53935;
}

.reservation-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
.reservation-card {
    display: grid;
    grid-template-columns: 200px 1fr auto;
    gap: 20px;
    align-items: flex-start;
    border: 1px solid #E0E0E0;
    border-radius: 12px;
    padding: 20px;
    transition: box-shadow 0.2s;
    background-color: #fff;
}
.reservation-card:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.reservation-card.disabled {
  opacity: 0.5;
}
.card-image {
    width: 200px;
    height: 140px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
}
.card-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.card-info {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
}
.place-name {
    font-size: 1.3rem;
    font-weight: 700;
    margin: 0 0 10px 0;
}
.reservation-details {
    font-size: 0.95rem;
    color: #555;
    line-height: 1.6;
}
.reservation-details span {
    display: block;
    margin-bottom: 4px;
}
.reservation-actions {
    margin-left: auto;
    text-align: right;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
}
.status-badge {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    display: inline-block;
}
.status-badge.예약-완료 {
    background-color: #E8F5E9;
    color: #388E3C;
}
.status-badge.reviewed, .status-badge.리뷰-작성-완료 {
    background-color: #e0e0e0;
    color: #757575;
    font-weight: 500;
}
.dates {
    font-size: 0.9rem;
    color: #333;
    font-weight: 500;
}
.price-people {
    font-size: 0.95rem;
    font-weight: 500;
}
.price-people span {
    display: block;
}
.btn-review-write {
    background-color: #4A69A1;
    color: #fff;
    border: none;
    padding: 8px 15px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 600;
}
.btn-review-write:hover {
    background-color: #3A5280;
}

.empty-state {
    text-align: center;
    padding: 50px 20px;
    color: #999;
    font-size: 1rem;
    background-color: #F8F4EF;
    border-radius: 12px;
}

.review-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
.review-card {
    border: 1px solid #E0E0E0;
    border-radius: 12px;
    padding: 20px;
}
.review-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}
.review-hotel-name {
    font-weight: 700;
    font-size: 1.1rem;
}
.review-date {
    font-size: 0.9rem;
    color: #888;
}
.review-rating {
    margin-bottom: 10px;
    color: #FFC107;
}
.review-rating .star {
    font-size: 1.2rem;
}
.review-content {
    color: #555;
    line-height: 1.6;
}

.modal-overlay {
    position: fixed;
    top: 0; left: 0;
    width: 100%; height: 100%;
    background-color: rgba(0,0,0,0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}
.modal-content {
    background-color: #fff;
    padding: 30px;
    border-radius: 12px;
    width: 90%;
    max-width: 500px;
}
.modal-title {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0 0 10px 0;
}
.modal-hotel-info {
    background-color: #f5f5f5;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
}
.review-form .form-group {
    margin-bottom: 15px;
}
.review-form label {
    display: block;
    font-weight: 500;
    margin-bottom: 8px;
}
.star-rating {
    font-size: 2rem;
    color: #ddd;
    cursor: pointer;
}
.star-rating .filled {
    color: #FFC107;
}
.review-form textarea {
    width: 100%;
    box-sizing: border-box;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 8px;
    resize: vertical;
}
.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}
.btn-cancel, .btn-submit {
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
}
.btn-cancel {
    background-color: #f0f0f0;
}
.btn-submit {
    background-color: #4A69A1;
    color: white;
}
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 500px;
  margin-top: 20px;
}
.form-group {
  text-align: left;
}
.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #555;
}
.form-input {
    width: 100%;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
}
.input-with-button {
    display: flex;
    gap: 10px;
    align-items: center;
}
.input-with-button input {
    flex-grow: 1;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
}
.btn-update-field {
    padding: 12px 15px;
    background-color: #4A69A1;
    color: #fff;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    white-space: nowrap;
    font-weight: 600;
}
.password-change-section {
    margin-top: 20px;
    border-top: 1px solid #eee;
    padding-top: 20px;
}
.password-section-title {
    font-size: 1.2rem;
    font-weight: 700;
    margin-bottom: 15px;
}

.btn-cancel-reservation {
  background-color: #ff4141;
  color: #fff;
  border: none;
  padding: 8px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  margin-top: 8px;
}

.btn-delete-cancelled {
  background-color: #888;
  color: #fff;
  border: none;
  padding: 8px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  margin-top: 8px;
}

.btn-delete-cancelled:hover {
  background-color: #555;
}
.mini-map { display: none; }
</style>