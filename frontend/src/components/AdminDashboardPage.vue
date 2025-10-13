<template>
  <div class="admin-dashboard">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <p class="admin-mode">사이트 관리자</p>
      </div>
      <nav class="sidebar-nav">
        <a @click="activeView = 'dashboard'" :class="{ active: activeView === 'dashboard' }">📊 대시보드</a>
        <a @click="activeView = 'approvals'" :class="{ active: activeView === 'approvals' }">✅ 매니저 / 호텔 승인</a>
        <a @click="activeView = 'hotels'" :class="{ active: activeView === 'hotels' }">🏨 호텔 관리</a>
        <a @click="activeView = 'users'" :class="{ active: activeView === 'users' }">👥 사용자 관리</a>
        <a @click="activeView = 'reviews'" :class="{ active: activeView === 'reviews' }">📝 리뷰 삭제 요청</a>
        <a @click="activeView = 'inquiries'" :class="{ active: activeView === 'inquiries' }">📞 문의 관리</a>
        <a @click="activeView = 'coupons'" :class="{ active: activeView === 'coupons' }">🎟️ 쿠폰 관리</a>
        <a @click="activeView = 'account'" :class="{ active: activeView === 'account' }">👤 계정 관리</a>
      </nav>
      <div class="sidebar-footer">
        <button @click="logout" class="btn-logout">로그아웃</button>
      </div>
    </aside>

    <main class="admin-content">

      <section v-if="activeView === 'dashboard'" class="content-section">
        <header class="content-header">
          <h1>사이트 전체 현황</h1>
          <p>플랫폼의 주요 지표를 확인합니다.</p>
        </header>
        <div class="dashboard-grid">
          <div class="card"><h4>총 플랫폼 매출</h4><p class="metric">{{ siteSales.totalRevenue.toLocaleString() }}원</p></div>
          <div class="card"><h4>월간 수수료 수익</h4><p class="metric">{{ siteSales.monthlyFee.toLocaleString() }}원</p></div>
          <div class="card"><h4>오늘 예약 건수</h4><p class="metric">{{ siteSales.todayBookings.toLocaleString() }}건</p></div> 
          <div class="card"><h4>신규 호텔 승인 대기</h4><p class="metric">{{ siteSales.newHotelPending.toLocaleString() }}개</p></div>
        </div>
        <div class="chart-grid">
          <div class="card chart-card">
              <RegionalSalesDistribution />
          </div>
          <div class="card chart-card">
              <MonthlySalesTrend />
          </div>
      </div>
      </section>

      <section v-if="activeView === 'approvals'" class="content-section">
        <header class="content-header">
          <h1>매니저 가입 / 호텔 등록 승인</h1>
          <p>승인 대기 중인 호텔 매니저 계정과 호텔을 관리합니다.</p>
        </header>

        <div class="tabs">
          <button @click="approvalView = 'MANAGER'" :class="{ active: approvalView === 'MANAGER' }">매니저 승인</button>
          <button @click="approvalView = 'HOTEL'" :class="{ active: approvalView === 'HOTEL' }">호텔 승인</button>
        </div>

        <div v-if="approvalView === 'MANAGER'" class="card">
          <table>
            <thead>
              <tr>
                <th>신청자명</th>
                <th>이메일</th>
                <th>상호명</th>
                <th>사업자 번호</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in pendingManagers" :key="user.id">
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.companyName || '정보 없음' }}</td>
                <td>{{ user.businessNumber || '정보 없음' }}</td>
                <td class="action-cell">
                  <button @click="handleApproveManager(user.id)" class="btn-sm btn-approve">가입 승인</button>
                  <button @click="handleRejectManager(user.id)" class="btn-sm btn-danger">승인 취소</button>
                </td>
              </tr>
              <tr v-if="!pendingManagers || pendingManagers.length === 0">
                <td colspan="5" class="no-results">승인 대기 중인 매니저가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else-if="approvalView === 'HOTEL'" class="card">
          <div class="bulk-actions">
            <button @click="handleApproveAllHotels" class="btn-sm btn-approve">전체 승인</button>
            <button @click="handleRejectAllHotels" class="btn-sm btn-danger">전체 거절</button>
          </div>
          <table>
            <thead>
              <tr>
                <th>호텔명</th>
                <th>지역</th>
                <th>타입</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="hotel in pendingHotels" :key="hotel.hId">
                <td>{{ hotel.hName }}</td>
                <td>{{ hotel.region }}</td>
                <td>{{ hotel.type }}</td>
                <td class="action-cell">
                  <button @click="handleApproveHotel(hotel.hId)" class="btn-sm btn-approve">등록 승인</button>
                  <button @click="handleRejectHotel(hotel.hId)" class="btn-sm btn-danger">등록 거절</button>
                </td>
              </tr>
              <tr v-if="!pendingHotels || pendingHotels.length === 0">
                <td colspan="4" class="no-results">승인 대기 중인 호텔이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeView === 'hotels'" class="content-section">
        <header class="content-header">
          <h1>전체 호텔 관리</h1>
          <p>등록된 모든 숙소의 상태를 관리하고 필터링합니다.</p>
        </header>

        <div class="card filter-controls">
          <div class="search-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search" viewBox="0 0 16 16"><path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/></svg>
            <input type="text" v-model="hotelSearchQuery" placeholder="호텔명, 지역으로 검색..." class="search-input">
          </div>
          <div class="checkbox-group">
            <label v-for="type in hotelTypes" :key="type">
              <input type="checkbox" :value="type" v-model="selectedHotelTypes"> {{ type }}
            </label>
          </div>
        </div>

        <div class="card">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>카테고리</th>
                <th>호텔명</th>
                <th>주소</th>
                <th>월간 매출</th>
                <th>평점</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="hotel in filteredHotels" :key="hotel.hId">
                <td>{{ hotel.hId }}</td>
                <td>{{ hotel.type }}</td>
                <td>{{ hotel.hName }}</td>
                <td>{{ hotel.region }}</td>
                <td>{{ hotel.monthlyRevenue.toLocaleString() }}원</td>
                <td>★ {{ hotel.averageRating.toFixed(1) }} ({{ hotel.bookingCount }}건)</td>
                <td>
                  <label class="switch">
                    <input type="checkbox" 
                      :checked="hotel.active" 
                       @change="handleHotelStatusChange(hotel, $event.target.checked)">
                    <span class="slider round"></span>
                  </label>
                </td>
              </tr>
              <tr v-if="!filteredHotels || filteredHotels.length === 0">
                <td colspan="7" class="no-results">조건에 맞는 호텔이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeView === 'users'" class="content-section">
        <header class="content-header">
          <h1>사용자 관리</h1>
          <p>플랫폼에 가입한 모든 사용자를 관리합니다.</p>
        </header>
        <div class="tabs">
          <button @click="userView = 'USER'" :class="{ active: userView === 'USER' }">일반 회원</button>
          <button @click="userView = 'MANAGER'" :class="{ active: userView === 'MANAGER' }">매니저 회원</button>
        </div>
        
        <div class="card filter-controls">
            <div class="search-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search" viewBox="0 0 16 16"><path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/></svg>
                <input type="text" v-model="userSearchQuery" @keyup.enter="fetchAllUsers" placeholder="이름 또는 이메일로 검색..." class="search-input">
            </div>
            <button @click="fetchAllUsers" class="btn-sm btn-approve">검색</button>
        </div>

        <div class="card">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>이름</th>
                <th>이메일</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.email }}</td>
                <td class="action-cell">
                  <button @click="openCouponIssueModal(user)" class="btn-sm btn-approve">쿠폰 지급</button>
                  <button @click="handleDeleteUser(user.id)" class="btn-sm btn-danger">계정 삭제</button>
                </td>
              </tr>
              <tr v-if="!filteredUsers || filteredUsers.length === 0">
                <td colspan="4" class="no-results">해당 조건의 사용자가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeView === 'reviews'" class="content-section">
        <header class="content-header">
          <h1>리뷰 삭제 요청</h1>
          <p>매니저가 요청한 리뷰 삭제 건을 심사하고 관리합니다.</p>
        </header>
        <div class="card">
          <table>
  <tbody>
    <tr v-for="req in pendingReviewRequests" :key="req.id">
      <td>{{ req.id }}</td>
      <td>{{ req.review?.userName || '정보 없음' }}</td>
      <td class="review-content-cell">"{{ req.review?.content || '-' }}"</td>
      <td class="reason-cell">{{ req.reason }}</td>
      <td class="action-cell">
        <button @click="handleApproveDeletion(req.id)" class="btn-sm btn-approve">삭제 승인</button>
        <button @click="handleRejectDeletion(req.id)" class="btn-sm btn-reject">요청 반려</button>
      </td>
    </tr>
    
    <tr v-if="!pendingReviewRequests || pendingReviewRequests.length === 0">
      <td colspan="5" class="no-results">접수된 리뷰 삭제 요청이 없습니다.</td>
    </tr>
  </tbody>
</table>
        </div>
      </section>

      <section v-if="activeView === 'inquiries'" class="content-section">
        <div v-if="!selectedInquiry">
          <h2 class="section-title">문의 목록</h2>
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>공개여부</th> <th>제목</th>
                  <th>작성자</th>
                  <th>상태</th>
                  <th>작성일</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="inquiry in inquiries" :key="inquiry.id" @click="selectInquiry(inquiry)">
                  <td>{{ inquiry.id }}</td>
                  <td>{{ inquiry.secret ? '비공개' : '공개' }}</td> <td>{{ inquiry.title }}</td>
                  <td>{{ inquiry.authorName }}</td>
                  <td>
                    <span class="status-badge" :class="inquiry.status.toLowerCase()">
                      {{ inquiry.status === 'ANSWERED' ? '답변완료' : '대기중' }}
                    </span>
                  </td>
                  <td>{{ new Date(inquiry.createdAt).toLocaleDateString() }}</td>
                </tr>
                <tr v-if="inquiries.length === 0">
                  <td colspan="5" class="no-data">새로운 문의가 없습니다.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else class="detail-view">
          <div class="detail-header">
            <h2 class="section-title">문의 상세 및 답변</h2>
            <button class="btn-back" @click="selectedInquiry = null">← 목록으로 돌아가기</button>
          </div>

          <div class="detail-content">
            <div class="info-grid">
              <div><strong>제목:</strong> {{ selectedInquiry.title }}</div>
              <div><strong>작성자:</strong> {{ selectedInquiry.authorName }} ({{ selectedInquiry.member ? '회원' : '비회원' }})</div>
              <div><strong>작성일:</strong> {{ new Date(selectedInquiry.createdAt).toLocaleString() }}</div>
              <div><strong>연락처 이메일:</strong> {{ selectedInquiry.email }}</div>
            </div>
            
            <div class="content-box question">
              <h4>문의 내용</h4>
              <p>{{ selectedInquiry.content }}</p>
            </div>

            <div v-if="selectedInquiry.status === 'ANSWERED'" class="content-box answer">
              <h4>등록된 답변</h4>
              <p>{{ selectedInquiry.answer }}</p>
              <p class="answer-date">답변일: {{ new Date(selectedInquiry.answeredAt).toLocaleString() }}</p>
            </div>

            <form v-else @submit.prevent="submitAnswer" class="answer-form">
              <h4>답변 작성</h4>
              <textarea v-model="adminAnswer" rows="8" placeholder="답변을 입력하세요..."></textarea>
              <div class="form-actions">
                <button type="submit" class="btn-submit">답변 등록</button>
              </div>
            </form>
          </div>
        </div>
      </section>

      <section v-if="activeView === 'coupons'" class="content-section">
          <header class="content-header">
            <h1>쿠폰 관리</h1>
            <p>새로운 쿠폰을 생성하거나 특정 사용자에게 쿠폰을 발급합니다.</p>
          </header>

          <div class="card">
            <h4>새 쿠폰 생성</h4>
            <form @submit.prevent="createNewCoupon" class="coupon-form">
              <input type="text" v-model="newCoupon.name" placeholder="쿠폰 이름 (예: 여름 휴가 15% 할인)" required>
              <select v-model="newCoupon.type" required>
                <option value="PERCENT">정률(%)</option>
                <option value="FIXED">정액(원)</option>
              </select>
              <input type="number" v-model.number="newCoupon.value" placeholder="할인 값 (예: 15 또는 10000)" required>
              <input type="date" v-model="newCoupon.expiryDate" required>
              <button type="submit" class="btn-sm btn-approve">쿠폰 생성</button>
            </form>
          </div>

          <div class="card">
            <h4>쿠폰 타입 관리</h4>
            <table>
              <thead>
                <tr>
                  <th>쿠폰 ID</th>
                  <th>쿠폰 이름</th>
                  <th>타입</th>
                  <th>할인 값</th>
                  <th>만료일</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="coupon in allCouponTypes" :key="coupon.id">
                  <td>{{ coupon.id }}</td>
                  <td>{{ coupon.name }}</td>
                  <td>{{ coupon.type }}</td>
                  <td>{{ coupon.discountValue }}</td>
                  <td>{{ coupon.expiryDate }}</td>
                  <td class="action-cell">
                    <button @click="issueToAllUsers(coupon.id)" class="btn-sm btn-approve">전체 지급</button>
                    <button @click="deleteCouponType(coupon.id)" class="btn-sm btn-danger">타입 삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="card">
            <h4>쿠폰 지급 내역</h4>
            <table>
                <thead>
                    <tr>
                        <th>지급 ID</th>
                        <th>사용자 이름</th>
                        <th>이메일</th>
                        <th>쿠폰 이름</th>
                        <th>지급일</th>
                        <th>관리</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="issued in issuedCoupons" :key="issued.userCouponId">
                        <td>{{ issued.userCouponId }}</td>
                        <td>{{ issued.userName }}</td>
                        <td>{{ issued.userEmail }}</td>
                        <td>{{ issued.couponName }}</td>
                        <td>{{ formatDateTime(issued.issuedAt) }}</td>
                        <td class="action-cell">
                            <button @click="deleteIssuedCoupon(issued.userCouponId)" class="btn-sm btn-danger">지급 삭제</button>
                        </td>
                    </tr>
                </tbody>
            </table>
          </div>
      </section>
      
      <section v-if="activeView === 'account'" class="content-section">
        <header class="content-header">
          <h1>계정 관리</h1>
          <p>현재 로그인된 관리자 계정 정보를 확인하고 관리합니다.</p>
        </header>

        <div class="card">
          <h4>로그인 정보</h4>
          <div class="account-info">
            <p><strong>이름:</strong> {{ authStore.userName }}</p>
            <p><strong>이메일:</strong> {{ authStore.userEmail }}</p>
          </div>
        </div>

        <div class="card">
          <h4>비밀번호 변경</h4>
          <form @submit.prevent="handleChangePassword" class="password-form">
            <div class="form-group">
              <label for="current-password">현재 비밀번호</label>
              <input type="password" id="current-password" v-model="passwordData.currentPassword" required>
            </div>
            <div class="form-group">
              <label for="new-password">새 비밀번호</label>
              <input type="password" id="new-password" v-model="passwordData.newPassword" required>
            </div>
            <div class="form-group">
              <label for="confirm-password">새 비밀번호 확인</label>
              <input type="password" id="confirm-password" v-model="passwordData.confirmPassword" required>
            </div>
            <button type="submit" class="btn-sm btn-approve">비밀번호 변경</button>
          </form>
        </div>
      </section>
    </main>

    <div v-if="isCouponIssueModalVisible" class="modal-overlay" @click.self="closeCouponIssueModal">
      <div class="modal-content">
        <header class="modal-header">
          <h2>쿠폰 지급</h2>
          <button @click="closeCouponIssueModal" class="btn-close">×</button>
        </header>
        <div class="modal-body">
          <p><strong>To:</strong> {{ selectedUserForCoupon.name }} ({{ selectedUserForCoupon.email }})</p>
          <div class="form-group">
            <label for="coupon-select">쿠폰 선택</label>
            <select id="coupon-select" v-model="couponIdToIssue">
              <option disabled value="">발급할 쿠폰을 선택하세요</option>
              <option v-for="coupon in allCouponTypes" :key="coupon.id" :value="coupon.id">
                {{ coupon.name }}
              </option>
            </select>
          </div>
        </div>
        <footer class="modal-footer">
          <button @click="closeCouponIssueModal" class="btn-sm btn-reject">취소</button>
          <button @click="handleIssueCoupon" class="btn-sm btn-approve">발급</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, reactive } from 'vue';
import axios from 'axios';
import MonthlySalesTrend from '@/components/MonthlySalesTrend.vue';
import RegionalSalesDistribution from '@/components/RegionalSalesChart.vue';
import { useAdminStore } from '@/stores/adminStore';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const adminStore = useAdminStore();

const router = useRouter();
const authStore = useAuthStore();

const activeView = ref('dashboard');
const allUsers = ref([]);
const allHotels = ref([]);
const reviewDeletionRequests = ref([]);
const pendingReviewRequests = computed(() =>
  reviewDeletionRequests.value.filter(req => req.status === 'PENDING')
);
const userView = ref('USER');
const issuedCoupons = ref([]);
const couponUserSearch = ref('');
const searchedUsers = ref([]);
const isCouponIssueModalVisible = ref(false);
const selectedUserForCoupon = ref(null);
const couponIdToIssue = ref('');

const hotelSearchQuery = ref('');
const selectedHotelTypes = ref([]);
const userSearchQuery = ref('');

// --- [추가] 문의 관리 관련 상태 변수 ---
const inquiries = ref([]);
const selectedInquiry = ref(null);
const adminAnswer = ref('');

// --- [추가] 문의 목록 데이터 로드 함수 ---
const fetchInquiries = async () => {
  try {
    const response = await axios.get('/api/inquiries', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    });
    inquiries.value = response.data;
  } catch (error) {
    console.error('문의 목록을 불러오는 데 실패했습니다:', error);
    alert('문의 목록을 불러오는 데 실패했습니다.');
  }
};

// --- [추가] 문의 선택 함수 ---
const selectInquiry = (inquiry) => {
  selectedInquiry.value = inquiry;
  adminAnswer.value = ''; // 답변 필드 초기화
};

// --- [추가] 답변 제출 함수 ---
const submitAnswer = async () => {
  if (!adminAnswer.value.trim()) {
    alert('답변 내용을 입력해주세요.');
    return;
  }
  if (!selectedInquiry.value) return;

  try {
    const inquiryId = selectedInquiry.value.id;
    await axios.post(`/api/inquiries/${inquiryId}/answer`, { answer: adminAnswer.value }, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    });
    alert('답변이 성공적으로 등록되었습니다.');
    // 목록 새로고침 및 상세 뷰로 복귀
    await fetchInquiries();
    // 방금 답변한 문의의 최신 정보를 다시 선택하여 보여줌
    const updatedInquiry = inquiries.value.find(i => i.id === inquiryId);
    if (updatedInquiry) {
      selectInquiry(updatedInquiry);
    } else {
      selectedInquiry.value = null; // 혹시 모를 경우를 대비해 목록으로
    }
  } catch (error) {
    console.error('답변 등록에 실패했습니다:', error);
    alert('답변 등록에 실패했습니다.');
  }
};

const passwordData = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const siteSales = ref({
  totalRevenue: 0,
  monthlyFee: 0,
  todayBookings: 0,
  newHotelPending: 0
});

const newCoupon = reactive({
  name: '',
  type: 'PERCENT',
  value: null,
  expiryDate: ''
});

const issueData = reactive({
  userId: null,
  couponId: ''
});

const allCouponTypes = ref([]);

const hotelTypes = computed(() => {
    if (!allHotels.value) return [];
    return [...new Set(allHotels.value.map(hotel => hotel.type))];
});

const filteredHotels = computed(() => {
    if (!allHotels.value) return [];
    return allHotels.value.filter(hotel => {
        const query = hotelSearchQuery.value.toLowerCase();
        const matchesSearch = hotel.hName.toLowerCase().includes(query) ||
                              hotel.region.toLowerCase().includes(query);
        const matchesType = selectedHotelTypes.value.length === 0 || selectedHotelTypes.value.includes(hotel.type);
        return matchesSearch && matchesType;
    });
});

const pendingManagers = computed(() =>
  allUsers.value.filter(user => user.role === 'ROLE_PENDING')
);

const filteredUsers = computed(() => {
  const roleToFilter = userView.value === 'USER' ? 'ROLE_USER' : 'ROLE_MANAGER';
  return allUsers.value.filter(user => user.role === roleToFilter);
});

const getAuthHeaders = () => ({
  headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
});

const fetchDashboardKpis = async () => {
  try {
    const accessToken = localStorage.getItem('accessToken');
    const headers = { 'Authorization': `Bearer ${accessToken}` };
    
    // API 호출을 Promise.all로 병렬 처리 (가독성 및 성능 향상)
    const [totalRevenueRes, monthlyFeeRes, todayBookingsRes, pendingHotelRes] = await Promise.all([
        axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/trend/total-revenue`, { headers }),
        axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/trend/commission/current-month `, { headers }),
        axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/trend/count/today`, { headers }),
        axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/trend/hotel/pending-count`, { headers }),
    ]);

    siteSales.value = {
      totalRevenue: totalRevenueRes.data, 
      monthlyFee: monthlyFeeRes.data,
      todayBookings: todayBookingsRes.data,
      newHotelPending: pendingHotelRes.data,
    };
  } catch (error) {
    console.error('대시보드 KPI 로드 중 오류 발생:', error);
  }
};


const fetchAllUsers = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/users`, {
        ...getAuthHeaders(),
        params: {
            role: 'ALL',
            searchQuery: userSearchQuery.value
        }
    });
    allUsers.value = response.data;
  } catch (error) {
    console.error("사용자 목록 조회 실패:", error);
  }
};

const fetchAllHotels = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/all`, getAuthHeaders());
    allHotels.value = response.data;
  } catch (error) {
    console.error("전체 호텔 목록 조회 실패:", error);
  }
};

const fetchReviewDeletionRequests = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/reviews/deletion-requests`, getAuthHeaders());
    reviewDeletionRequests.value = response.data;
  } catch (error) {
    console.error("리뷰 삭제 요청 목록 조회 실패:", error);
  }
};

const fetchAllCouponTypes = async () => {
    try {
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons`, getAuthHeaders());
        allCouponTypes.value = response.data;
    } catch (error) {
        console.error("전체 쿠폰 종류 조회 실패:", error);
    }
};

const handleApproveManager = async (userId) => {
  if (confirm('이 매니저의 가입을 승인하시겠습니까?')) {
    try {
      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/users/${userId}/approve`, {}, getAuthHeaders());
      alert('매니저 가입이 승인되었습니다.');
      await fetchAllUsers();
    } catch (error) {
      alert('매니저 승인 처리 중 오류가 발생했습니다.');
    }
  }
};

const handleRejectManager = async (userId) => {
  if (confirm('이 매니저의 가입 신청을 거절(삭제)하시겠습니까?')) {
    try {
      await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/admin/users/${userId}`, getAuthHeaders());
      alert('매니저 가입 신청이 거절(삭제)되었습니다.');
      await fetchAllUsers();
    } catch (error) {
      alert('매니저 승인 취소 처리 중 오류가 발생했습니다.');
    }
  }
};

const approvalView = ref('MANAGER');

// 승인 대기 호텔 필터링
const pendingHotels = computed(() =>
  allHotels.value.filter(hotel => hotel.status === '대기')
  
);

console.log(allHotels.value);
console.log(allHotels.value.map(h => h.status));

const handleApproveHotel = async (hotelId) => {
  if (confirm('이 호텔을 승인하시겠습니까?')) {
    try {
      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/approve/${hotelId}`, {}, getAuthHeaders());
      alert('호텔이 승인되었습니다.');
      await fetchAllHotels();
    } catch (error) {
      alert('호텔 승인 처리 중 오류 발생');
    }
  }
};

const handleRejectHotel = async (hotelId) => {
  if (confirm('이 호텔의 신청을 거절하시겠습니까?')) {
    try {
      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/reject/${hotelId}`, {}, getAuthHeaders());
      alert('호텔 신청이 거절되었습니다.');
      await fetchAllHotels();
    } catch (error) {
      alert('호텔 승인 취소 처리 중 오류 발생');
    }
  }
};

// 전체 호텔 승인 로직
const handleApproveAllHotels = async () => {
  const pendingHotelIds = pendingHotels.value.map(h => h.hId);
  if (pendingHotelIds.length === 0) {
    alert('승인 대기 중인 호텔이 없습니다.');
    return;
  }
  if (confirm(`승인 대기 중인 ${pendingHotelIds.length}개의 호텔을 모두 승인하시겠습니까?`)) {
    try {
      // Promise.all을 사용하여 모든 요청을 병렬로 처리
      const approvalPromises = pendingHotelIds.map(hotelId =>
        axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/approve/${hotelId}`, {}, getAuthHeaders())
      );
      await Promise.all(approvalPromises);
      alert('모든 대기 중인 호텔이 승인되었습니다.');
      await fetchAllHotels(); // 목록 새로고침
    } catch (error) {
      alert('일부 또는 전체 호텔 승인 처리 중 오류가 발생했습니다.');
      console.error('전체 호텔 승인 오류:', error);
    }
  }
};

// 전체 호텔 거절 로직
const handleRejectAllHotels = async () => {
  const pendingHotelIds = pendingHotels.value.map(h => h.hId);
  if (pendingHotelIds.length === 0) {
    alert('승인 대기 중인 호텔이 없습니다.');
    return;
  }
  if (confirm(`승인 대기 중인 ${pendingHotelIds.length}개의 호텔 신청을 모두 거절하시겠습니까?`)) {
    try {
      const rejectionPromises = pendingHotelIds.map(hotelId =>
        axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/reject/${hotelId}`, {}, getAuthHeaders())
      );
      await Promise.all(rejectionPromises);
      alert('모든 대기 중인 호텔 신청이 거절되었습니다.');
      await fetchAllHotels(); // 목록 새로고침
    } catch (error) {
      alert('일부 또는 전체 호텔 신청 거절 처리 중 오류가 발생했습니다.');
      console.error('전체 호텔 거절 오류:', error);
    }
  }
};

const handleDeleteUser = async (userId) => {
  if (confirm(`사용자 ID ${userId}를 정말로 삭제하시겠습니까?`)) {
    try {
      await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/admin/users/${userId}`, getAuthHeaders());
      alert('사용자가 삭제되었습니다.');
      await fetchAllUsers();
    } catch (error) {
      alert('사용자 삭제 중 오류가 발생했습니다.');
    }
  }
};

const handleHotelStatusChange = async (hotel, isChecked) => {
  // isChecked는 이미 true/false boolean 값입니다.
  const newStatus = isChecked;
  
  // 원래의 boolean 상태를 저장합니다.
  const originalStatus = hotel.active;

  // UI를 먼저 boolean 값으로 업데이트합니다.
  hotel.active = newStatus;

  try {
    // 서버 API에 { active: true } 또는 { active: false } 형태의 데이터를 전송합니다.
    await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/admin/hotels/${hotel.hId}/status`, 
      { active: newStatus },
      getAuthHeaders()
    );
    // 성공 시: UI는 이미 업데이트되었으므로 추가 작업이 필요 없습니다.

  } catch (error) {
    alert('호텔 상태 변경 중 오류가 발생했습니다.');
    // 실패 시: UI를 원래의 boolean 상태로 되돌립니다.
    hotel.active = originalStatus;
  }
};

const handleApproveDeletion = async (requestId) => {
  if (confirm('정말로 이 리뷰를 영구적으로 삭제하시겠습니까?')) {
    try {
      await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/admin/reviews/deletion-requests/${requestId}`, getAuthHeaders());
      alert("리뷰가 삭제되었습니다.");
      await fetchReviewDeletionRequests();
    } catch (error) {
      alert("리뷰 삭제 처리 중 오류가 발생했습니다.");
    }
  }
};

const handleRejectDeletion = async (requestId) => {
  const reason = prompt("삭제 요청을 반려하는 사유를 입력해주세요.");
  if (reason?.trim()) {
    try {
      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/reviews/deletion-requests/${requestId}/reject`, { reason: reason.trim() }, getAuthHeaders());
      alert("삭제 요청이 반려 처리되었습니다.");
      await fetchReviewDeletionRequests();
    } catch (error) {
      alert("요청 반려 처리 중 오류가 발생했습니다.");
    }
  } else if (reason !== null) {
    alert("반려 사유를 반드시 입력해야 합니다.");
  }
};

const handleChangePassword = async () => {
  if (passwordData.newPassword !== passwordData.confirmPassword) {
    alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
    return;
  }
  if (passwordData.newPassword.length < 8) {
      alert('새 비밀번호는 8자 이상이어야 합니다.');
      return;
  }

  try {
    const response = await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/admin/account/password`, {
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword
    }, getAuthHeaders());

    alert(response.data.message);
    passwordData.currentPassword = '';
    passwordData.newPassword = '';
    passwordData.confirmPassword = '';
  } catch (error) {
    alert(error.response?.data?.message || '비밀번호 변경에 실패했습니다.');
  }
};

const issueCouponToUser = async () => {
    if (!issueData.userId || !issueData.couponId) {
        alert('사용자 ID와 발급할 쿠폰을 모두 선택해주세요.');
        return;
    }
    try {
        await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/issue`, {
            userId: issueData.userId,
            couponId: issueData.couponId
        }, getAuthHeaders());
        alert(`${issueData.userId}번 사용자에게 쿠폰이 발급되었습니다.`);
        Object.assign(issueData, { userId: null, couponId: '' });
        await fetchIssuedCoupons();
    } catch (error) {
        console.error('쿠폰 발급 실패:', error);
        alert('쿠폰 발급에 실패했습니다. 사용자 ID나 쿠폰 ID를 확인해주세요.');
    }
};

const issueToAllUsers = async (couponId) => {
  if (confirm('모든 사용자에게 이 쿠폰을 발급하시겠습니까?')) {
    try {
      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/issue-all`, { couponId }, getAuthHeaders());
      alert('모든 사용자에게 쿠폰이 발급되었습니다.');
      await fetchIssuedCoupons();
    } catch (error) {
      alert('쿠폰 전체 발급 중 오류가 발생했습니다.');
    }
  }
};

const deleteCouponType = async (couponId) => {
  if (confirm('이 쿠폰 타입을 삭제하면, 발급된 모든 관련 쿠폰이 영구적으로 삭제됩니다. 계속하시겠습니까?')) {
    try {
      await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/${couponId}`, getAuthHeaders());
      alert('쿠폰 타입이 삭제되었습니다.');
      await fetchAllCouponTypes();
      await fetchIssuedCoupons();
    } catch (error) {
      alert('쿠폰 타입 삭제 중 오류가 발생했습니다.');
    }
  }
};

const createNewCoupon = async () => {
  try {
    await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons`, newCoupon, getAuthHeaders());
    alert('새 쿠폰이 생성되었습니다.');
    await fetchAllCouponTypes();
    Object.assign(newCoupon, { name: '', type: 'PERCENT', value: null, expiryDate: '' });
  } catch (error) {
    alert('쿠폰 생성 중 오류가 발생했습니다.');
  }
};

const fetchIssuedCoupons = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/issued`, getAuthHeaders());
    issuedCoupons.value = response.data;
  } catch (error) {
    console.error("지급된 쿠폰 목록 조회 실패:", error);
  }
};

const deleteIssuedCoupon = async (userCouponId) => {
    if (confirm('이 지급 내역을 삭제하시겠습니까?')) {
        try {
            await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/issued/${userCouponId}`, getAuthHeaders());
            alert('쿠폰 지급 내역이 삭제되었습니다.');
            await fetchIssuedCoupons();
        } catch (error) {
            alert('쿠폰 지급 내역 삭제 중 오류가 발생했습니다.');
        }
    }
};

const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ko-KR');
};

const searchUsersForCoupon = async () => {
    if (!couponUserSearch.value) {
        searchedUsers.value = [];
        return;
    }
    try {
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/admin/users`, {
            ...getAuthHeaders(),
            params: {
                role: 'USER',
                searchQuery: couponUserSearch.value
            }
        });
        searchedUsers.value = response.data;
    } catch (error) {
        console.error("쿠폰 발급을 위한 사용자 검색 실패:", error);
    }
};

const selectUserForCoupon = (user) => {
    issueData.userId = user.id;
    couponUserSearch.value = `${user.name} (${user.email})`;
    searchedUsers.value = [];
};

const openCouponIssueModal = (user) => {
    selectedUserForCoupon.value = user;
    couponIdToIssue.value = '';
    isCouponIssueModalVisible.value = true;
};

const closeCouponIssueModal = () => {
    isCouponIssueModalVisible.value = false;
};

const handleIssueCoupon = async () => {
    if (!couponIdToIssue.value) {
        alert('발급할 쿠폰을 선택해주세요.');
        return;
    }
    try {
        await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/admin/coupons/issue`, {
            userId: selectedUserForCoupon.value.id,
            couponId: couponIdToIssue.value
        }, getAuthHeaders());
        alert(`${selectedUserForCoupon.value.name}님에게 쿠폰이 발급되었습니다.`);
        closeCouponIssueModal();
        await fetchIssuedCoupons();
    } catch (error) {
        alert('쿠폰 발급에 실패했습니다.');
    }
};

const logout = () => {
    authStore.logout();
};

watch(activeView, (newView) => {
  if (newView === 'approvals' || newView === 'users') {
    userSearchQuery.value = '';
    fetchAllUsers();
    fetchAllHotels();
  } else if (newView === 'reviews') {
    fetchReviewDeletionRequests();
  } else if (newView === 'dashboard') {
    fetchDashboardKpis();
  } else if (newView === 'hotels') {
    fetchAllHotels();
  } else if (newView === 'coupons') {
    fetchAllCouponTypes();
    fetchIssuedCoupons();
  } else if (newView === 'inquiries') {
    // 문의 관리 탭으로 전환 시, 목록을 불러오고 상세 뷰는 닫음
    selectedInquiry.value = null;
    fetchInquiries();
  } else if (newView === 'account') {
    if (!authStore.userName) {
        authStore.fetchUserInfo();
    }
  }
});

onMounted(() => {
  fetchDashboardKpis();
  if (activeView.value === 'managerApproval' || activeView.value === 'approvals') {
        fetchAllUsers();
    }
});

watch(userView, () => {
    userSearchQuery.value = '';
    fetchAllUsers();
});



</script>

<style scoped>
/* 기존 스타일은 유지 */
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700;800&display=swap');

.admin-dashboard { display: grid; grid-template-columns: 260px 1fr; height: 100vh; font-family: 'Noto Sans KR', sans-serif; }
.admin-sidebar { background-color: #1F2937; color: white; display: flex; flex-direction: column; padding: 25px; }
.sidebar-header .logo { font-family: 'Nanum Myeongjo', serif; font-size: 1.8rem; color: white; text-decoration: none; cursor: pointer; }
.sidebar-header .admin-mode {
  background-color: #4F46E5;
  padding: 10px 20px;  /* 상하/좌우 패딩 늘리면 큼직해짐 */
  border-radius: 8px;
  font-size: 1.2rem;   /* 글자도 같이 키움 */
  display: inline-block;
  margin-top: 10px;
}

.sidebar-nav { margin-top: 40px; display: flex; flex-direction: column; gap: 10px; }
.sidebar-nav a { color: #D1D5DB; text-decoration: none; font-size: 1.1rem; padding: 15px 20px; border-radius: 8px; cursor: pointer; }
.sidebar-nav a.active, .sidebar-nav a:hover { background-color: #374151; color: #fff; }
.sidebar-footer { margin-top: auto; }
.btn-logout { width: 100%; padding: 12px; background-color: #D9534F; color: white; border: none; font-size: 1rem; border-radius: 8px; cursor: pointer; }

.content-header { margin-bottom: 30px; }
.content-header h1 { font-size: 2.5rem; font-weight: 800; color: #111827; margin: 0; }
.content-header p { font-size: 1.1rem; color: #6B7280; margin-top: 5px; }
.card { background-color: #fff; border-radius: 12px; padding: 25px; margin-bottom: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 15px; text-align: left; border-bottom: 1px solid #eee; }
th { font-weight: 600; color: #6B7280; font-size: 0.9rem; }
td { vertical-align: middle; }
.review-content-cell { max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.reason-cell { max-width: 250px; white-space: pre-wrap; word-break: keep-all; }
.action-cell { display: flex; gap: 10px; }
.no-results { text-align: center; color: #888; padding: 40px; font-size: 1.1rem; }
.btn-sm { padding: 6px 12px; font-size: 0.85rem; border-radius: 6px; border: none; cursor: pointer; font-weight: 600; }
.btn-approve { background-color: #10B981; color: white; }
.btn-reject { background-color: #F59E0B; color: white; }
.btn-danger { background-color: #EF4444; color: white; }
.content-section { flex-grow: 1; display: flex; flex-direction: column; gap: 25px; }
.dashboard-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 25px; }
.card .metric { font-size: 2.5rem; font-weight: 700; color: #111827; margin: 10px 0 0 0; }
.chart-grid { flex-grow: 1; display: grid; grid-template-columns: 3fr 2fr; gap: 25px; min-height: 0; }
.chart-card { height: 100%; display: flex; flex-direction: column; }
.tabs { margin-bottom: 20px; display: flex; gap: 10px; border-bottom: 1px solid #ddd; }
.tabs button { background: none; border: none; padding: 15px 20px; font-size: 1.1rem; font-weight: 600; color: #6B7280; cursor: pointer; border-bottom: 3px solid transparent; }
.tabs button.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.switch{position:relative;display:inline-block;width:50px;height:28px}.switch input{opacity:0;width:0;height:0}.slider{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background-color:#ccc;transition:.4s}.slider:before{position:absolute;content:"";height:20px;width:20px;left:4px;bottom:4px;background-color:#fff;transition:.4s}input:checked+.slider{background-color:#4F46E5}input:checked+.slider:before{transform:translateX(22px)}.slider.round{border-radius:34px}.slider.round:before{border-radius:50%}

/* [수정 및 추가] 필터 및 검색바 스타일 */
.filter-controls {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    align-items: center;
    padding: 20px;
    background-color: #f9fafb;
    border-radius: 8px;
}
.search-wrapper {
    position: relative;
    flex-grow: 1;
}
.search-wrapper .bi-search {
    position: absolute;
    top: 50%;
    left: 15px;
    transform: translateY(-50%);
    color: #9ca3af;
}
.search-input {
    padding: 12px 15px 12px 40px; /* 아이콘 공간 확보 */
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 1rem;
    width: 100%;
    box-sizing: border-box;
}
.checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    align-items: center;
}
.checkbox-group label {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    font-weight: 500;
}

/* 계정 관리 페이지 스타일 */
.account-info p {
  font-size: 1.1rem;
  margin-bottom: 10px;
}
.password-form {
  max-width: 400px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
}
.form-group input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
}

.admin-content { 
    background-color: #F9FAFB; 
    padding: 40px 40px 0 40px; 
    /* 🎯 1. Flex 컨테이너로 설정 (내부 요소를 수직으로 배치) */
    display: flex;
    flex-direction: column; 
    /* 🎯 2. 메인 컨텐츠 영역에 스크롤을 유지 */
    overflow-y: auto; 
    min-height: 0;
    height: 100%; 
}

.admin-content > .content-section {
    flex-grow: 1; 
    display: flex;
    flex-direction: column;
    /* padding-bottom: 40px; 👈 이 줄을 제거하고, 필요하다면 admin-content에 padding-bottom을 추가하세요. */
    gap: 25px; 
    min-height: 0;
}

.chart-grid {
    flex-grow: 1; /* 남은 수직 공간을 전부 차지 */
    /* 기존 grid-template-columns는 유지 (예: 3fr 2fr) */
    display: grid;
    grid-template-columns: 2fr 3fr; 
    gap: 25px; 
    /* 🎯 차트 카드가 늘어난 공간을 채우도록 height: 100% 설정 */
    /*height: 100%; */
    min-height: 0;
}
.chart-card {
    /* 기존 height: 550px; 대신 */
    flex-grow: 1; /* height: 100% */ 
    /* ... 나머지 flex 속성 유지 ... */
    display: flex; 
    flex-direction: column;
    min-height: 0;
}

/* 차트 컴포넌트가 부모 컨테이너 높이를 채우도록 설정 */
.chart-card > div:last-child {
    flex-grow: 1; 
}

.coupon-form {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr auto;
    gap: 15px;
    align-items: center;
}
.coupon-form input, .coupon-form select {
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 0.95rem;
}
.user-search-wrapper {
    position: relative;
}
.user-search-results {
    position: absolute;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 6px;
    list-style: none;
    margin-top: 5px;
    padding: 0;
    width: 100%;
    z-index: 10;
}
.user-search-results li {
    padding: 10px;
    cursor: pointer;
}
.user-search-results li:hover {
    background-color: #f0f0f0;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1001;
}
.modal-content {
    background-color: white;
    padding: 20px;
    border-radius: 8px;
    width: 400px;
}
.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    margin-bottom: 20px;
}
.modal-body .form-group {
    margin-bottom: 0;
}
.modal-body select {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
}
.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
}
.btn-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
}

.bulk-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 15px;
}

/* --- [추가] 문의 관리 관련 스타일 --- */
.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
  color: white;
}
.status-badge.pending { background-color: #ffc107; }
.status-badge.answered { background-color: #28a745; }

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.btn-back {
  background: none;
  border: 1px solid #ccc;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 0.95rem;
}
.content-box {
  padding: 20px;
  border-radius: 6px;
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;
}
.content-box h4 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 10px 0;
}
.content-box p {
  white-space: pre-wrap;
  line-height: 1.6;
  margin: 0;
}
.question { background-color: #f0f4ff; }
.answer { background-color: #f3f3f3; }
.answer-date {
  text-align: right;
  font-size: 0.85rem;
  color: #888;
  margin-top: 15px !important;
}
.answer-form textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  resize: vertical;
}
.form-actions {
  text-align: right;
  margin-top: 15px;
}
.btn-submit {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>