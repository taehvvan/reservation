<template>
  <div class="manager-dashboard">
    <aside class="manager-sidebar">
      <div class="sidebar-header">
        <a @click="goHome" class="logo">쉼, 한국</a>
        <p class="manager-mode">호텔 매니저</p>
      </div>
      <nav class="sidebar-nav">
        <a @click="setActiveView('dashboard')" :class="{ active: activeView === 'dashboard' }">📈 매출 관리</a>
        <a @click="setActiveView('reservations')" :class="{ active: activeView === 'reservations' }">📅 예약 관리</a>
        <a @click="setActiveView('accommodation')" :class="{ active: activeView === 'accommodation' }">🏨 숙소/객실 관리</a>
        <a @click="setActiveView('reviews')" :class="{ active: activeView === 'reviews' }">✍️ 리뷰 관리</a>
        <a @click="setActiveView('account')" :class="{ active: activeView === 'account' }">👤 계정 관리</a>
      </nav>
      <div class="sidebar-footer">
        <button class="btn-logout">로그아웃</button>
      </div>
    </aside>

    <main class="manager-content">
      <section v-show="activeView === 'dashboard'" class="content-section">
        <header class="content-header">
          <h1>매출 현황</h1>
          <p>기간별 매출과 점유율 현황을 확인합니다.</p>
        </header>

        <div class="dashboard-container">
          <div v-if="loading" class="flex justify-center items-center h-96">
            <div class="loading-spinner"></div>
            <p class="ml-4 text-gray-600">데이터를 불러오는 중...</p>
          </div>

          <div v-else>
            <div class="dashboard-grid">
              <div class="card metric-card">
                <h4>총 매출</h4>
                <p class="metric">{{ formatCurrency(metrics.totalSales) }}</p>
              </div>

              <div class="card metric-card">
                <h4>오늘 점유율</h4>
                <div class="metric-wrapper">
                  <p class="metric">{{ metrics.dailyOccupancy.toFixed(2) }}%</p>
                  <p
                    class="change-text"
                    :style="{ color: occupancyChange > 0 ? '#16a34a' : occupancyChange < 0 ? '#dc2626' : '#6b7280' }"
                  >
                    {{ occupancyChange > 0 ? '+' : '' }}{{ occupancyChange.toFixed(2) }}%
                  </p>
                </div>
              </div>

              <div class="card metric-card">
                <h4>오늘 매출</h4>
                <p class="metric">{{ formatCurrency(metrics.dailySales) }}</p>
              </div>
            </div>

            <div class="card mb-6">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-lg font-semibold">
                  매출 및 점유율 - {{ selectedHotel ? selectedHotel.hotelName : '전체 호텔' }}
                </h4>
                <div class="flex justify-end space-x-2">
                  <button
                    :class="{ active: mainChartPeriod === 'daily' }"
                    @click="setMainPeriod('daily')"
                  >일간</button>
                  <button
                    :class="{ active: mainChartPeriod === 'weekly' }"
                    @click="setMainPeriod('weekly')"
                  >주간</button>
                  <button
                    :class="{ active: mainChartPeriod === 'monthly' }"
                    @click="setMainPeriod('monthly')"
                  >월간</button>
                  <button
                    :class="{ active: mainChartPeriod === 'yearly' }"
                    @click="setMainPeriod('yearly')"
                  >연간</button>
                </div>
              </div>
              <Chart
                :key="mainChartPeriod + '-' + (selectedHotel?.hotelId || 'all')"
                type="bar"
                :data="mainChartData"
                :options="mainChartOptions"
                style="height: 400px;"
              />
            </div>

            <div class="card mb-6">
              <div class="flex flex-row gap-6 flex-nowrap justify-center">
                
                <div class="flex-1 flex flex-col items-center gap-4">
                  <h4 class="text-base font-semibold">호텔별 매출 비율</h4>
                  <div class="flex flex-row items-start gap-6 w-full">
                    <div class="w-20 h-20 sm:w-24 sm:h-24">
                      <Doughnut :data="donutData" :options="tinyDonutOptions" />
                    </div>
                    <ul
                      class="legend-list max-h-56 overflow-auto flex-1 grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 pr-2"
                    >
                      <li
                        v-for="(label, idx) in donutData.labels"
                        :key="label + idx"
                        class="flex items-center gap-2"
                      >
                        <span
                          class="legend-dot"
                          :style="{
                            backgroundColor: donutData.datasets[0].backgroundColor[
                              idx % donutData.datasets[0].backgroundColor.length
                            ]
                          }"
                        />
                        <span class="text-sm text-gray-700 truncate">{{ label }}</span>
                        <span class="ml-auto text-sm font-medium">
                          {{ formatCurrency(donutData.datasets[0].data[idx] || 0) }}
                        </span>
                      </li>
                    </ul>
                  </div>
                </div>

                <div
                  v-if="selectedHotel && selectedHotel.rooms?.length"
                  class="flex-1 flex flex-col items-center gap-4"
                >
                  <h4 class="text-base font-semibold">{{ selectedHotel.hotelName }} - 방별 매출 비율</h4>
                  <div class="flex flex-row items-start gap-6 w-full">
                    <div class="w-28 h-28">
                      <Doughnut
                        :key="selectedHotel?.hotelId || 'all'"
                        :data="roomDonutData"
                        :options="tinyDonutOptions"
                      />
                    </div>
                    <ul
                      class="legend-list max-h-56 overflow-auto flex-1 grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 pr-2"
                    >
                      <li
                        v-for="(room, idx) in selectedHotel.rooms"
                        :key="room.name + idx"
                        class="flex items-center gap-2"
                      >
                        <span
                          class="legend-dot"
                          :style="{
                            backgroundColor: roomDonutData.datasets[0].backgroundColor[
                              idx % roomDonutData.datasets[0].backgroundColor.length
                            ]
                          }"
                        />
                        <span class="text-sm text-gray-700 truncate">{{ room.name }}</span>
                        <span class="ml-auto text-sm font-medium">
                          {{ formatCurrency(room.sales) }}
                        </span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            <div class="card mb-6">
              <h4 class="text-lg font-semibold mb-4">호텔 목록</h4>

              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-4">
                <div
                  class="hotel-card card p-4 hover:bg-gray-100"
                  :class="{ active: !selectedHotel }"
                  @click="resetSelection"
                >
                  <h5 class="text-base font-medium">전체 보기</h5>
                  <p class="text-sm text-gray-500">모든 호텔 합산</p>
                </div>

                <div
                  v-for="hotel in hotels"
                  :key="hotel.hotelId"
                  class="hotel-card card p-4 hover:bg-gray-100"
                  :class="{ active: selectedHotel && selectedHotel.hotelId === hotel.hotelId }"
                  @click="selectHotel(hotel)"
                >
                  <h5 class="text-base font-medium truncate">{{ hotel.hotelName }}</h5>
                  <p class="text-sm text-gray-500 truncate">{{ hotel.address }}</p>
                  <p class="text-sm font-semibold">매출: {{ formatCurrency(hotel.sales.total) }}</p>
                  <p class="text-sm">오늘 매출: {{ formatCurrency(hotel.sales.daily) }}</p>
                  <p class="text-sm">오늘 점유율: {{ hotel.sales.occupancy.toFixed(2) }}%</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeView === 'reservations'" class="content-section">
        <header class="content-header">
          <h1>예약 관리</h1>
          <p>모든 숙소의 예약 현황을 확인하고 관리합니다.</p>
        </header>

        <div class="card search-card">
            <div class="search-controls">
                <select v-model="searchType" class="search-select">
                    <option value="guestName">고객 이름</option>
                    <option value="id">예약 번호</option>
                </select>
                <input type="text" v-model="searchQuery" :placeholder="searchPlaceholder" class="search-input">
            </div>
            <div class="status-filter-controls">
                <button @click="statusFilter = 'all'" :class="{ active: statusFilter === 'all' }">전체</button>
                <button v-for="option in statusOptions" :key="option.code"
                        @click="statusFilter = option.code"
                        :class="{ active: statusFilter === option.code }">
                    {{ option.text }}
                </button>
            </div>
        </div>

        <div class="card">
          <div class="filter-tabs">
            <button @click="userTypeFilter = 'all'" :class="{ active: userTypeFilter === 'all' }">
              전체 예약 ({{ filteredBySearchAndStatus.length }})
            </button>
            <button @click="userTypeFilter = 'member'" :class="{ active: userTypeFilter === 'member' }">
              회원 예약 ({{ memberReservations.length }})
            </button>
            <button @click="userTypeFilter = 'guest'" :class="{ active: userTypeFilter === 'guest' }">
              비회원 예약 ({{ guestReservations.length }})
            </button>
          </div>

          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>예약 번호</th>
                  <th>고객명</th>
                  <th>전화번호</th>
                  <th>숙소명</th>
                  <th>객실명</th>
                  <th>체크인</th>
                  <th>체크아웃</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="finalFilteredReservations.length === 0">
                  <td colspan="7" class="no-results">해당하는 예약 내역이 없습니다.</td>
                </tr>
                <tr v-for="booking in finalFilteredReservations" :key="booking.orderId">
                  <td>{{ booking.orderId }}</td>
                  <td>
                    <span :class="['user-type-badge', booking.isMember ? 'member' : 'guest']">
                      {{ booking.isMember ? '회원' : '비회원' }}
                    </span>
                    {{ booking.guestName }}
                  </td>
                  <td>{{ booking.phone }}</td>
                  <td>{{ booking.hotelName }}</td>
                  <td>{{ booking.roomName }}</td>
                  <td>{{ booking.checkIn }}</td>
                  <td>{{ booking.checkOut }}</td>
                  <td>
                    <span :class="['status-badge', `status-${booking.status.code}`]">{{ booking.status.text }}</span>
                  </td>
                  <td>
                    <button 
                      v-if="booking.status.code === 'confirmed'" 
                      @click="handleCancelReservation(booking)"
                      class="btn-cancel-reservation"
                    >
                      예약 취소
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeView === 'accommodation'" class="content-section">
          <div v-if="accommodationView === 'typeSelection'">
            <header class="content-header">
              <h1>숙소 유형 선택</h1>
              <p>관리할 숙소의 유형을 선택해주세요.</p>
            </header>
            <div class="property-type-grid">
              <div class="property-type-card" @click="selectPropertyType('호텔')">
                <span class="icon">🏨</span>
                <h3>호텔</h3>
                <p>{{ getPropertyCount('호텔') }}개 등록됨</p>
              </div>
              <div class="property-type-card" @click="selectPropertyType('모텔')">
                <span class="icon">🏩</span>
                <h3>모텔</h3>
                <p>{{ getPropertyCount('모텔') }}개 등록됨</p>
              </div>
              <div class="property-type-card" @click="selectPropertyType('펜션')">
                <span class="icon">🏡</span>
                <h3>펜션 & 풀빌라</h3>
                <p>{{ getPropertyCount('펜션') }}개 등록됨</p>
              </div>
              <div class="property-type-card" @click="selectPropertyType('게스트하우스')">
                  <span class="icon">🧑‍🤝‍🧑</span>
                  <h3>게스트하우스</h3>
                  <p>{{ getPropertyCount('게스트하우스') }}개 등록됨</p>
              </div>
              <div class="property-type-card" @click="selectPropertyType('한옥')">
                <span class="icon">🏯</span>
                <h3>한옥</h3>
                <p>{{ getPropertyCount('한옥') }}개 등록됨</p>
              </div>
              <div class="property-type-card" @click="selectPropertyType('관광호텔')">
                <span class="icon">🏢</span>
                <h3>관광호텔</h3>
                <p>{{ getPropertyCount('관광호텔') }}개 등록됨</p>
              </div>
            </div>
          </div>

          <div v-if="accommodationView === 'list'">
            <header class="content-header with-back-button">
              <button @click="accommodationView = 'typeSelection'" class="btn-back">‹ 뒤로</button>
              <div>
                <h1>{{ selectedPropertyType }} 목록</h1>
                <p>등록된 숙소를 확인하고 관리합니다.</p>
              </div>
            </header>
            <div class="property-list">
              <div 
                v-for="prop in filteredProperties" 
                :key="prop.id" 
                class="card property-card"
                :class="{ 'pending-card': prop.status === '대기' }"
                @click="editProperty(prop)"
              >
                <img 
                  :src="prop.image || 'https://placehold.co/400x200?text=No+Image'" 
                  class="property-image" 
                  alt="숙소 대표 이미지"
                >
                <div class="property-info">
                  <h4>{{ prop.name }}</h4>
                  <p>{{ prop.location }}</p>
                  <span v-if="prop.status === '대기'" class="status-badge">대기</span>
                </div>
                <div class="property-manage-footer">
                  <button class="btn-manage" @click.stop="editProperty(prop)">관리</button>
                  <button class="btn-delete" @click.stop="deleteProperty(prop.id, prop.name)">삭제</button>
                </div>
              </div>

              <div class="card property-card add-new-card" @click="addNewProperty">
                <span class="add-icon">+</span>
                <h4>새 {{ selectedPropertyType }} 추가하기</h4>
              </div>
            </div>
          </div>
          
          <div v-if="accommodationView === 'edit' && editableHotel" class="edit-form-wrapper">

            <input 
                type="file" 
                ref="fileInputRef" 
                @change="handleFileSelect" 
                style="display: none;"
                accept="image/*"
            >
            <header class="content-header with-back-button">

              <button @click="accommodationView = 'list'" class="btn-back">‹ 목록으로</button>
              <div>
                <h1>{{ editFormTitle }}</h1>
              </div>
            </header>
            
            <div class="edit-form-layout">
              <div class="form-main">
                <div class="card">
                  <h4>사진 관리</h4>
                  <div class="photo-management-grid">
                    <div class="main-photo">
                      <img :src="editableHotel.images && editableHotel.images[0]" v-if="editableHotel.images && editableHotel.images[0]">
                      <div v-else class="photo-placeholder">+</div>
                      <button class="btn-photo-edit" @click="triggerFileInput('main', 0)">수정</button>
                    </div>
                    <div class="sub-photo" v-for="i in 4" :key="i">
                      <img :src="editableHotel.images && editableHotel.images[i]" v-if="editableHotel.images && editableHotel.images[i]">
                      <div v-else class="photo-placeholder">+</div>
                      <button class="btn-photo-edit" @click="triggerFileInput('sub', i)">수정</button>
                      </div>
                    </div>
                </div>
                
                <div class="card">
                  <h4>기본 정보</h4>
                  <div class="form-grid">
                    <div class="form-group"><label>숙소 유형</label><input type="text" :value="selectedPropertyType" disabled></div>
                    <div class="form-group" v-if="selectedPropertyType === '호텔' || selectedPropertyType === '관광호텔'"><label>호텔 성급</label><input type="number" v-model.number="editableHotel.stars" min="1" max="5"></div>
                    <div class="form-group full-width"><label>숙소 이름 <span class="required">*</span></label><input type="text" v-model="editableHotel.name"></div>
                    <div class="form-group full-width address-group">
                      <label>숙소 위치 <span class="required">*</span></label>
                      <div class="address-input-wrapper">
                        <input type="text" v-model="editableHotel.location" placeholder="상세 주소를 입력하세요">
                        <button @click="searchAddress" class="btn-search-address">좌표 검색</button>
                      </div>
                    </div>
                    <div class="form-group"><label>위도</label><input type="text" v-model="editableHotel.latitude" placeholder="예: 37.5665" readonly></div>
                    <div class="form-group"><label>경도</label><input type="text" v-model="editableHotel.longitude" placeholder="예: 126.9780" readonly></div>
                  </div>
                </div>
                
                <div class="card">
                  <h4>객실 관리</h4>
                  <div v-for="(room, index) in editableHotel.rooms" :key="index" class="room-edit-card">
                    <div class="room-photo">
                      <img :src="room.image" v-if="room.image">
                      <div v-else class="photo-placeholder small">+</div>
                      <button class="btn-photo-edit small" @click="triggerFileInput('room', index)">수정</button>
                    </div>
                    <div class="room-inputs-grid">
                      <div class="form-group-small room-name-input">
                        <label>객실 종류</label>
                        <input type="text" v-model="room.type" placeholder="예: 스탠다드 더블">
                      </div>
                      <div class="form-group-small">
                        <label>판매가 (원)</label>
                        <input type="number" v-model.number="room.price" placeholder="150000">
                      </div>
                      <div class="form-group-small">
                        <label>총 객실 수</label>
                        <input type="number" v-model.number="room.count" placeholder="20">
                      </div>
                      <div class="form-group-small">
                        <label>최대 인원</label>
                        <input type="number" v-model.number="room.people" placeholder="2">
                      </div>
                      <div class="form-group-small">
                          <label>체크인 시간</label>
                          <input type="time" v-model="room.checkinTime">
                      </div>
                      <div class="form-group-small">
                          <label>체크아웃 시간</label>
                          <input type="time" v-model="room.checkoutTime">
                      </div>
                    </div>
                    <button @click="removeRoom(index)" class="btn-remove-room">-</button>
                  </div>
                  <button @click="addRoom" class="btn-add-room">+ 새 객실 추가</button>
                </div>

                <div class="card">
                  <h4>서비스 및 부대시설</h4>
                  <div class="amenities-checkbox-grid">
                    <label v-for="amenity in allAmenities" :key="amenity.serviceId">
                      <input 
                        type="checkbox" 
                        :value="amenity.serviceId" 
                        v-model="editableHotel.serviceIds"
                      > {{ amenity.serviceName }}
                    </label>
                  </div>
                </div>
              </div>
              
              <div class="form-sidebar">
                <div class="sticky-sidebar">
                  <div class="card save-panel">
                    <h4>저장 및 관리</h4>
                    <button class="btn-save" @click="saveChanges">변경사항 저장</button>
                    <button class="btn-cancel" @click="cancelChanges">수정 취소</button>
                    <button 
                      v-if="selectedPropertyForEdit" 
                      class="btn-delete-in-form" 
                      @click="deleteProperty(editableHotel.id, editableHotel.name)"
                    >
                      이 숙소 삭제하기
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section v-if="activeView === 'reviews'" class="content-section">
          <header class="content-header">
            <h1>리뷰 관리</h1>
            <p>고객 리뷰에 답글을 달거나 악성 리뷰를 관리합니다.</p>
          </header>
          <div class="review-list">
            <div v-for="review in reviews" :key="review.reviewId" class="card review-card">
              <div class="review-header">
                <div class="review-info">
                  <span class="review-user-name">{{ review.userName }}</span>
                  <span class="review-hotel-name">🏨 {{ review.hotelName }}</span>
                </div>
                <div class="review-meta">
                    <div class="review-rating">
                        <span v-for="i in 5" :key="i" :class="['star', { 'filled': i <= review.score }]">★</span>
                    </div>
                    <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                </div>
              </div>
              <p class="review-content">{{ review.content }}</p>

              <div v-if="review.deletionStatus === 'REJECTED'" class="deletion-status-section status-rejected">
                  <p class="status-header"><strong>삭제 요청 반려됨</strong></p>
                  <p class="rejection-reason"><strong>반려 사유:</strong> {{ review.rejectionReason }}</p>
              </div>
              <div v-else-if="review.deletionStatus === 'PENDING'" class="deletion-status-section status-pending">
                  <p class="status-header"><strong>삭제 요청 처리 중...</strong></p>
                  <p class="rejection-reason">관리자가 검토하고 있습니다.</p>
              </div>

              <div class="review-reply-section">
                <div v-if="review.reply" class="manager-reply">
                  <p class="reply-header">
                    <strong>사장님 댓글</strong>
                    <span>{{ formatDate(review.repliedAt) }}</span>
                  </p>
                  <p class="reply-content">{{ review.reply }}</p>
                </div>
                <div v-else class="reply-form">
                  <textarea v-model="review.replyInput" placeholder="답글을 작성하여 고객에게 감사를 표현하세요."></textarea>
                  <div class="reply-actions">
                    <button v-if="!review.deletionStatus" class="btn-delete-request" @click="openDeletionModal(review)">삭제 요청</button>
                    <button @click="submitReply(review)" :disabled="!review.replyInput" class="btn-submit-reply">답글 등록</button>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!reviews || reviews.length === 0" class="card no-reviews-card">
              <p>아직 작성된 리뷰가 없습니다.</p>
            </div>
          </div>
        </section>
        
        <section v-if="activeView === 'account'" class="content-section">
          <header class="content-header">
            <h1>계정 관리</h1>
            <p>현재 로그인된 매니저 계정 정보를 확인하고 관리합니다.</p>
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
      
      <div v-if="isDeletionModalVisible" class="modal-overlay" @click.self="closeDeletionModal">
        <div class="modal-content">
          <header class="modal-header">
            <h2>리뷰 삭제 요청</h2>
            <button class="btn-close" @click="closeDeletionModal">×</button>
          </header>
          <div class="modal-body">
            <div class="review-preview-card">
              <p><strong>작성자:</strong> {{ requestingReview.userName }}</p>
              <p class="review-text-in-modal">"{{ requestingReview.content }}"</p>
            </div>
            <div class="form-group">
              <label for="deletion-reason">삭제 요청 사유</label>
              <textarea id="deletion-reason" v-model="deletionReason" rows="5" placeholder="사이트 관리자가 납득할 수 있도록 삭제 요청 사유를 구체적으로 작성해주세요. (예: 허위 사실 유포, 비방, 욕설 등)"></textarea>
            </div>
          </div>
          <footer class="modal-footer">
            <button class="btn-cancel" @click="closeDeletionModal">취소</button>
            <button class="btn-submit-request" @click="submitDeletionRequest" :disabled="!deletionReason.trim()">요청 보내기</button>
          </footer>
        </div>
      </div>

    </div>
  </template>


<script setup lang="ts">
import { ref, Ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { Chart, Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  ArcElement,
} from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, BarElement, LineElement, CategoryScale, LinearScale, PointElement, ArcElement)
import { useAdminStore } from '@/stores/adminStore';
import { useAuthStore } from '@/stores/auth';

const adminStore = useAdminStore()

const authStore = useAuthStore();

const reviews = ref([]);
const isLoadingReviews = ref(false);
const managedHotelId = ref(''); 
const isDeletionModalVisible = ref(false);
const requestingReview = ref(null);
const deletionReason = ref('');

const hotels = ref<any[]>([])
const selectedHotel = ref<any | null>(null)
const summary = ref<any | null>(null)
const loading = ref<boolean>(true)
type Period = 'daily' | 'weekly' | 'monthly' | 'yearly'

const mainChartPeriod: Ref<Period> = ref('daily')
const hotelChartPeriod: Ref<Period> = ref('daily')

const contentRef = ref<HTMLElement | null>(null)

const router = useRouter();
const activeView = ref('reservations'); // 기본 뷰를 'reservations'로 설정
const accommodationView = ref('typeSelection');
const selectedPropertyType = ref(null);
const selectedPropertyForEdit = ref(null);
const editableHotel = ref(null);
const fileInputRef = ref(null);
const imageUpdateTarget = ref({ type: null, index: null });
const selectedMetric = ref('monthly');
const isBookingModalVisible = ref(false);
const imageFiles = ref([]);

const passwordData = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// --- 예약 관리 관련 상태 ---
const reservations = ref([]); // 서버에서 받은 원본 예약 목록
const searchQuery = ref('');
const searchType = ref('guestName');
const statusFilter = ref('all');
const userTypeFilter = ref('all'); // [추가] 회원/비회원 필터 상태 ('all', 'member', 'guest')

const statusOptions = ref([
  { code: 'confirmed', text: '예약 완료' },
  { code: 'cancelled', text: '예약 취소' },
  { code: 'reviewed', text: '리뷰 작성 완료' }
]);

const managedProperties = ref([]);
const propertyTypeMapping = {
  '호텔': 'Hotel',
  '모텔': 'Motel',
  '펜션': 'Pension',
  '게스트하우스': 'Guesthouse',
  '관광호텔': 'Tourist Hotel'
};

const allAmenities = ref([]);

const scrollToTop = () => {
  if (contentRef.value) {
    contentRef.value.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const setMainPeriod = (p: Period) => {
  mainChartPeriod.value = p
  updateMainChart()
}

const setHotelPeriod = (p: Period) => {
  hotelChartPeriod.value = p
  updateHotelChart()
}

const tinyDonutOptions = reactive<any>({
  responsive: false,
  maintainAspectRatio: false,
  cutout: '60%',
  plugins: {
    legend: { display: false }, // 커스텀 범례 사용
    tooltip: { enabled: true }
  }
})


// =========================
// 메트릭
// =========================
const metrics = computed(() => ({
  totalSales: summary.value?.totalSales ?? 0,
  dailySales: summary.value?.dailySales ?? 0,
  dailyOccupancy: summary.value?.dailyOccupancy ?? 0,
  yesterdayOccupancy: summary.value?.yesterdayOccupancy ?? 0
}))

const formatCurrency = (amount: number) =>
  amount ? amount.toLocaleString('ko-KR') + '원' : '0원'

// =========================
// 차트 데이터 구조
// =========================
const mainChartData = reactive<any>({
  labels: [],
  datasets: [
    {
      type: 'line',
      label: '평균 방 점유율 (%)',
      data: [],
      borderColor: '#1cc88a',
      backgroundColor: '#1cc88a',
      yAxisID: 'y1',
      borderWidth: 3,
      pointRadius: 6,
      pointHoverRadius: 8,
      tension: 0.3
    },
    {
      type: 'bar',
      label: '총 매출',
      data: [],
      backgroundColor: '#4e73df',
      yAxisID: 'y'
    }
  ]
})

const mainChartOptions = reactive<any>({
  responsive: true,
  plugins: {
    legend: { position: 'top' },
    tooltip: { mode: 'index', intersect: false }
  },
  interaction: { mode: 'index', intersect: false },
  scales: {
    y: {
      type: 'linear',
      position: 'left',
      ticks: { callback: (v: number) => `${Number(v).toLocaleString('ko-KR')}원` }
    },
    y1: {
      type: 'linear',
      position: 'right',
      grid: { drawOnChartArea: false },
      ticks: { callback: (v: number) => `${v}%` }
    }
  }
})

const hotelChartData = reactive<any>({
  labels: [],
  datasets: [
    {
      type: 'line',
      label: '방 점유율 (%)',
      data: [],
      borderColor: '#e74a3b',
      backgroundColor: '#e74a3b',
      yAxisID: 'y1',
      borderWidth: 3,
      pointRadius: 6,
      pointHoverRadius: 8,
      tension: 0.3
    },
    {
      type: 'bar',
      label: '총 매출',
      data: [],
      backgroundColor: '#36b9cc',
      yAxisID: 'y'
    }
  ]
})

const hotelChartOptions = reactive<any>({
  responsive: true,
  plugins: { legend: { position: 'top' } },
  scales: {
    y: {
      type: 'linear',
      position: 'left',
      ticks: { callback: (v: number) => `${Number(v).toLocaleString('ko-KR')}원` }
    },
    y1: {
      type: 'linear',
      position: 'right',
      grid: { drawOnChartArea: false },
      ticks: { callback: (v: number) => `${v}%` }
    }
  }
})

const occupancyChange = computed(() => {
  const today = metrics.value.dailyOccupancy
  const yesterday = metrics.value.yesterdayOccupancy
  return today - yesterday
})

// =========================
// 호텔 선택
// =========================
const selectHotel = (hotel: any) => {
  selectedHotel.value = hotel
  summary.value = {
    totalSales: hotel.sales?.total ?? 0,
    dailySales: hotel.sales?.daily ?? 0,
    dailyOccupancy: hotel.sales?.occupancy ?? 0,
    yesterdayOccupancy: 0
  }

  updateHotelChart()
  updateMainChart()   // 🟢 추가

  // 🟢 스크롤 맨 위로 이동
  window.scrollTo({ top: 0, behavior: 'smooth' })
  scrollToTop() // ✅ 내부 스크롤 맨 위로

}

const resetSelection = () => {
  selectedHotel.value = null
  summary.value = dataCache.summary
  donutData.labels = dataCache.donut.labels
  donutData.datasets[0].data = dataCache.donut.sales

  // 🟢 방별 차트도 초기화
  roomDonutData.labels = []
  roomDonutData.datasets[0].data = []

  updateMainChart()

  // 🟢 스크롤 맨 위로 이동
  window.scrollTo({ top: 0, behavior: 'smooth' })
  scrollToTop() // ✅ 내부 스크롤 맨 위로
}

// =========================
// 도넛
// =========================
const donutData = reactive<any>({
  labels: [],
  datasets: [{ data: [], backgroundColor: ['#36A2EB', '#FF6384', '#FFCE56', '#4BC0C0'] }]
})

const donutOptions = reactive<any>({
  responsive: false,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' }
  }
})

// =========================
// 차트 업데이트
// =========================
const updateMainChart = () => {
  if (selectedHotel.value?.chart) {
    // 🟢 특정 호텔만 보여주기
    const chart = selectedHotel.value.chart[mainChartPeriod.value] ?? { labels: [], sales: [], occupancy: [] }
    mainChartData.labels = [...(chart.labels ?? [])]
    mainChartData.datasets[0].data = [...(chart.occupancy ?? [])]
    mainChartData.datasets[1].data = [...(chart.sales ?? [])]
    return
  }

  // 🟢 전체 호텔 합산 (선택 안했을 때)
  if (!hotels.value?.length) return
  const base = hotels.value[0]?.chart?.[mainChartPeriod.value]
  if (!base?.labels) return

  const labels = [...base.labels]
  const sales = Array(labels.length).fill(0)
  const occAgg = Array(labels.length).fill(0)
  let occDivisor = 0

  hotels.value.forEach(h => {
    const c = h.chart?.[mainChartPeriod.value]
    if (c?.sales?.length === labels.length && c?.occupancy?.length === labels.length) {
      c.sales.forEach((v: number, i: number) => (sales[i] += v ?? 0))
      c.occupancy.forEach((v: number, i: number) => (occAgg[i] += v ?? 0))
      occDivisor++
    }
  })

  mainChartData.labels = labels
  mainChartData.datasets[0].data = occAgg.map(v => occDivisor ? v / occDivisor : 0)
  mainChartData.datasets[1].data = sales
}

const updateHotelChart = () => {
  if (!selectedHotel.value?.chart) return
  const chart = selectedHotel.value.chart[hotelChartPeriod.value] ?? { labels: [], sales: [], occupancy: [] }
  hotelChartData.labels = [...(chart.labels ?? [])]
  hotelChartData.datasets[0].data = [...(chart.occupancy ?? [])]
  hotelChartData.datasets[1].data = [...(chart.sales ?? [])]
}

// =========================
// API
// =========================
let dataCache: any = {}

const fetchData = async () => {
  
  const accessToken = localStorage.getItem('accessToken');
    const headers = { 'Authorization': `Bearer ${accessToken}` };
    
  try {
    const res = await axios.get('/api/mchart/my-info', { headers})
    console.log('📊 받은 데이터:', res.data)
    const data = res.data

    summary.value = data.summary
    hotels.value = data.hotels
    donutData.labels = data.donut.labels
    donutData.datasets[0].data = data.donut.sales

    dataCache = data
    selectedHotel.value = null
    updateMainChart()
  } finally {
    loading.value = false
  }
}

const roomDonutData = reactive<any>({
  labels: [],
  datasets: [{
    data: [],
    backgroundColor: ['#36A2EB','#FF6384','#FFCE56','#4BC0C0','#9966FF','#FF9F40']
  }]
})

// selectedHotel 바뀔 때마다 갱신
watch(selectedHotel, (hotel) => {
  if (hotel?.rooms) {
    roomDonutData.labels = hotel.rooms.map((r: any) => r.name)
    roomDonutData.datasets[0].data = hotel.rooms.map((r: any) => r.sales)
  } else {
    roomDonutData.labels = []
    roomDonutData.datasets[0].data = []
  }
})

watch([selectedHotel, mainChartPeriod], () => {
  updateMainChart()
})

onMounted(fetchData);

// --- [핵심 수정] 필터링 로직 ---

// 1. 검색어와 상태로 1차 필터링
const filteredBySearchAndStatus = computed(() => {
  let tempReservations = reservations.value;

  // 상태 필터링
  if (statusFilter.value !== 'all') {
    tempReservations = tempReservations.filter(booking => booking.status.code === statusFilter.value);
  }

  // 검색어 필터링
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    tempReservations = tempReservations.filter(booking => {
      if (searchType.value === 'guestName') {
        return booking.guestName.toLowerCase().includes(query);
      }
      if (searchType.value === 'id') { // <--- 수정된 코드
          // [추가] 예약 번호(orderId)와 예약 ID(reservationId)를 모두 검색하도록 개선
          return String(booking.orderId).includes(query) || 
                String(booking.reservationId).includes(query);
      }
      return false;
    });
  }

  return tempReservations;
});

// 2. 회원/비회원으로 2차 필터링 (1차 필터링된 결과 기반)
const memberReservations = computed(() => 
  filteredBySearchAndStatus.value.filter(r => r.isMember)
);
const guestReservations = computed(() => 
  filteredBySearchAndStatus.value.filter(r => !r.isMember)
);

// 3. 최종적으로 화면에 보여줄 목록
const finalFilteredReservations = computed(() => {
  if (userTypeFilter.value === 'member') {
    return memberReservations.value;
  }
  if (userTypeFilter.value === 'guest') {
    return guestReservations.value;
  }
  return filteredBySearchAndStatus.value; // 'all'
});
// ------------------------------

const searchPlaceholder = computed(() => {
  return searchType.value === 'guestName' ? '고객 이름으로 검색...' : '예약 번호로 검색...';
});

const canceledBookingsCount = computed(() => {
  return reservations.value.filter(b => b.status.code === 'cancelled').length;
});

// --- 데이터 로딩 함수 ---
const fetchManagedHotels = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    });

    // --- 안전장치 추가 ---
    // 서버에서 받은 데이터가 배열인지 확인합니다.
    if (Array.isArray(response.data)) {
      managedProperties.value = response.data;
    } else {
      // 배열이 아니라면, 콘솔에 경고를 출력하고 빈 배열로 초기화하여 오류를 방지합니다.
      console.warn('API로부터 배열이 아닌 데이터가 수신되었습니다:', response.data);
      managedProperties.value = [];
    }
  } catch (error) {
    console.error('호텔 목록을 불러오는 데 실패했습니다:', error);
    managedProperties.value = []; // 오류 발생 시에도 안전하게 빈 배열로 설정
    alert('등록된 호텔 정보를 가져오는 데 실패했습니다.');
  }
};

const fetchReservations = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/manager/reservations`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
    });

    reservations.value = response.data.map(r => ({
      reservationId: r.reservationId,
      orderId: r.orderId,
      guestName: r.userId === null ? '' : r.guestName,
      phone: r.phone,
      hotelName: r.hotelName,
      roomName: r.roomType,
      checkIn: r.checkIn,
      checkOut: r.checkOut,
      isMember: r.userId !== null, // userId 존재 여부로 회원/비회원 구분
      status: {
        code: r.status === '예약 완료' ? 'confirmed'
        : r.status === '리뷰 작성 완료' ? 'reviewed'
        : 'cancelled',
        text: r.status
      }
    }));
  } catch (error) {
    console.error('예약 내역을 불러오는 데 실패했습니다:', error);
    alert('예약 정보를 가져올 수 없습니다.');
  }
};

const fetchAllAmenities = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/services`, {
          headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        allAmenities.value = response.data;
    } catch (error) {
        console.error('서비스 목록을 불러오는 데 실패했습니다:', error);
    }
};

const fetchReviews = async () => {
  isLoadingReviews.value = true;
  try {
    // 수정된 API 엔드포인트('/api/manager/reviews')로 GET 요청
    const response = await axios.get('/api/manager/reviews', {
      headers: { 
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}` 
      }
    });
    reviews.value = response.data;
  } catch (error) {
    // console.error("리뷰 목록을 불러오는 데 실패했습니다:", error);
    // alert("리뷰 정보를 가져오는 중 오류가 발생했습니다.");
  } finally {
    isLoadingReviews.value = false;
  }
};

const handleCancelReservation = async (booking) => {
  const isConfirmed = confirm(`[예약번호: ${booking.orderId}] ${booking.guestName}님의 예약을 정말로 취소하시겠습니까?`);
  
  if (isConfirmed) {
    try {
      const token = localStorage.getItem('accessToken');
      // HTTP 메서드를 delete로 변경합니다.
      const response = await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/manager/reservations/${booking.reservationId}/cancel`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      alert(response.data.message || '예약이 성공적으로 취소되었습니다.');
      
      await fetchReservations();

    } catch (error) {
      console.error('예약 취소 실패:', error);
      const errorMessage = error.response?.data?.message || '예약 취소 중 오류가 발생했습니다.';
      alert(errorMessage);
    }
  }
};

// --- 뷰 변경 및 초기화 ---
const setActiveView = (viewName) => {
  activeView.value = viewName;
  if (viewName === 'dashboard') {
    fetchData();
  } else if (viewName === 'reservations') {
    fetchReservations();
  } else if (viewName === 'accommodation') {
    accommodationView.value = 'typeSelection';
    fetchManagedHotels();
    fetchAllAmenities(); // 숙소 관리 탭 진입 시 부대시설 목록도 미리 로드
  } else if (viewName === 'reviews') {
    fetchReviews();
  }
};

onMounted(() => {
  // 페이지 로드 시 현재 뷰에 맞는 데이터 로드
  setActiveView(activeView.value);
});


// ---------------- 이하 코드는 기존과 동일합니다 ----------------

const getPropertyCount = (koreanType) => {
  // --- 안전장치 추가 ---
  // managedProperties.value가 배열이 아닐 경우를 대비해 기본값 0을 반환합니다.
  if (!Array.isArray(managedProperties.value)) {
    return 0;
  }

  const englishType = propertyTypeMapping[koreanType];
  if (!englishType) return 0;
  return managedProperties.value.filter(p => p.type === englishType).length;
};

const filteredProperties = computed(() => {
  if (!selectedPropertyType.value) return [];
  // 사용자가 선택한 한글 유형을 영문 유형으로 변환합니다.
  const englishType = propertyTypeMapping[selectedPropertyType.value];
  if (!englishType) return [];
  // 변환된 영문 유형으로 필터링합니다.
  return managedProperties.value.filter(p => p.type === englishType);
});

const selectPropertyType = (type) => {
  selectedPropertyType.value = type;
  accommodationView.value = 'list';
};

const getAuthHeaders = () => ({
  headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
});

const salesData = {
  all: { total: 125800000, monthly: 32500000, daily: 1200000, todayBookings: 7 },
  '호텔': { monthly: 15000000, daily: 600000 },
  '펜션': { monthly: 8000000, daily: 350000 },
  '한옥': { monthly: 5500000, daily: 150000 },
  '관광호텔': { monthly: 4000000, daily: 100000 },
};
const salesFilterType = ref('all');
const salesFilterTypes = [
  { key: 'all', text: '전체' },
  { key: '호텔', text: '🏨 호텔' },
  { key: '펜션', text: '🏡 펜션' },
  { key: '한옥', text: '🏯 한옥' },
  { key: '관광호텔', text: '🏢 관광호텔' },
];
const filteredSales = computed(() => {
  const type = salesFilterType.value;
  if (type === 'all' || !salesData[type]) {
    return salesData.all;
  }
  return {
    total: salesData.all.total,
    monthly: salesData[type].monthly,
    daily: salesData[type].daily,
    todayBookings: salesData.all.todayBookings
  };
});
const todayBookingsDetails = ref([
  { id: 1, hotelName: '쉼, 서울 호텔', roomName: '스탠다드 더블', guestName: '김예약' },
  { id: 2, hotelName: '오션뷰, 부산 펜션', roomName: '오션뷰 스파', guestName: '이바다' },
]);

const managerAccount = ref({ companyName: '(주)쉼호텔', businessNumber: '123-45-67890' });


const chartTitle = computed(() => {
  const filterText = salesFilterType.value === 'all' ? '전체' : salesFilterType.value;
  if (selectedMetric.value === 'total') return `총 매출 상세 분석`;
  if (selectedMetric.value === 'monthly') return `${filterText} 월별 매출 추이`;
  if (selectedMetric.value === 'daily') return `${filterText} 일별 매출 상세`;
  return '매출 현황';
});
const monthlySalesData = {
  labels: ['4월', '5월', '6월', '7월', '8월', '9월'],
  all: [28000000, 35000000, 31000000, 42000000, 51000000, 32500000],
  '호텔': [12000000, 16000000, 14000000, 18000000, 22000000, 15000000],
};
const chartData = computed(() => {
  const type = salesFilterType.value;
  const data = monthlySalesData[type] || monthlySalesData.all;
  return {
    labels: monthlySalesData.labels,
    datasets: [{
        label: `${type === 'all' ? '전체' : type} 매출 (원)`,
        backgroundColor: '#3498DB',
        borderRadius: 6,
        data: data,
    },],
  };
});

// [추가] 파일이 선택되었을 때 실행될 함수
const handleFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) {
    return; // 파일 선택을 취소한 경우
  }

  console.log('선택된 파일:', file);
  // `imageUpdateTarget.value`를 통해 어떤 이미지를 업데이트할지 알 수 있습니다.
  console.log('업데이트 대상 정보:', imageUpdateTarget.value);

  // 여기에 선택된 파일을 미리보기에 표시하거나
  // 서버로 업로드하는 로직을 구현합니다.
  
  // input 값을 초기화하여 같은 파일을 다시 선택할 수 있도록 함
  event.target.value = ''; 
};

// ★★★★★ 2. 객실 사진 오류 수정 ★★★★★
const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // type과 index를 imageUpdateTarget에서 가져옵니다.
  const { type, index } = imageUpdateTarget.value;

  const reader = new FileReader();
  reader.onload = (e) => {
    const imageUrl = e.target.result as string; // Base64 데이터 URL

    if (type === 'room') {
      // ✅ 객실 사진일 경우: 해당 객실의 image 속성을 업데이트합니다.
      // 이 경우, 이미지는 JSON 데이터에 포함되어 Base64 문자열로 전송됩니다.
      if (editableHotel.value && editableHotel.value.rooms[index]) {
        editableHotel.value.rooms[index].image = imageUrl;
      }
    } else {
      // ✅ 숙소 대표/서브 사진일 경우: images 배열을 업데이트합니다.
      if (!editableHotel.value.images) {
        editableHotel.value.images = [];
      }
      editableHotel.value.images[index] = imageUrl;

      // ✅ 이 사진들은 '저장' 시 별도로 업로드해야 하므로 파일 목록에 추가합니다.
      if (!imageFiles.value) imageFiles.value = [];
      imageFiles.value[index] = file;
    }
  };

  reader.readAsDataURL(file);
  event.target.value = '';
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
    const response = await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/manager/account/password`, {
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


const saveChanges = async () => {
  if (!editableHotel.value) return;
  if (!editableHotel.value.name || editableHotel.value.name.trim() === '') {
    alert('숙소 이름은 필수 항목입니다.');
    return;
  }

  const token = localStorage.getItem('accessToken');
  const isNewProperty = !selectedPropertyForEdit.value;

  try {
    if (isNewProperty) {
      // --- 신규 등록 ---
      const formData = new FormData();
      const hotelData = { ...editableHotel.value };
      delete hotelData.images;
      delete hotelData.id;
      
      formData.append('hotelDto', new Blob([JSON.stringify(hotelData)], { type: 'application/json' }));
      
      // imageFiles 배열에 있는 (null이 아닌) 파일만 FormData에 추가합니다.
      imageFiles.value.filter(file => file).forEach(file => {
        formData.append('images', file);
      });

      await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels`, formData, {
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' }
      });
      alert('새로운 호텔이 등록되었습니다. 사이트 관리자가 검토 후 승인 후 검색에 노출됩니다.');

    } else {
      // --- 기존 숙소 수정 ---
      const hotelId = editableHotel.value.id;

      // 1. 텍스트 정보 먼저 업데이트 (PUT)
      await axios.put(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels/${hotelId}`, editableHotel.value, {
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
      });

      // 2. 새로 선택된 이미지가 있으면, 이미지 업로드 (POST)
      const validImageFiles = imageFiles.value.filter(file => file);
      if (validImageFiles.length > 0) {
        const imageFormData = new FormData();
        validImageFiles.forEach(file => imageFormData.append('images', file));
        await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels/${hotelId}/images`, imageFormData, {
          headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'multipart/form-data' }
        });
      }
      alert('변경사항이 성공적으로 저장되었습니다.');
    }

    await fetchManagedHotels();
    accommodationView.value = 'list';

  } catch (error) {
    console.error('숙소 정보 저장 실패:', error);
    alert(`오류가 발생했습니다: ${error.response?.data?.message || error.message}`);
  }
};

const deleteProperty = async (hotelId, hotelName) => {
  if (!confirm(`'${hotelName}' 숙소를 정말로 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`)) {
    return;
  }

  try {
    const token = localStorage.getItem('accessToken');
    await axios.delete(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels/${hotelId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    alert('숙소가 성공적으로 삭제되었습니다.');
    
    // 목록 뷰에 있었다면 목록 새로고침, 수정 뷰에 있었다면 목록으로 이동하며 새로고침
    await fetchManagedHotels();
    if (accommodationView.value === 'edit') {
      accommodationView.value = 'list';
    }

  } catch (error) {
    console.error('숙소 삭제 실패:', error);
    alert(`삭제 중 오류가 발생했습니다: ${error.response?.data?.message || error.message}`);
  }
};

const cancelChanges = () => {
  accommodationView.value = 'list';
};

const addRoom = () => {
  if (editableHotel.value) {
    editableHotel.value.rooms.push({
      id: Date.now(),
      // ★★★★★ 'name'을 'type'으로 변경 ★★★★★
      type: '', 
      price: 0,
      count: 1,
      people: 2,
      active: true,
      image: '',
      checkinTime: '15:00',
      checkoutTime: '11:00'
    });
  }
};

// ★★★★★ 1. 체크박스 오류 수정 ★★★★★
const editProperty = async (property) => {
  try {
    const token = localStorage.getItem('accessToken');
    const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/manager/hotels/${property.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    const data = response.data;
    // API 응답에서 serviceIds가 null일 경우, 빈 배열로 만들어줍니다.
    data.serviceIds = data.serviceIds || [];

    selectedPropertyForEdit.value = data;
    editableHotel.value = JSON.parse(JSON.stringify(data));
    accommodationView.value = 'edit';
    imageFiles.value = []; // 이미지 파일 선택 목록 초기화
    
  } catch (error) {
    console.error('호텔 상세 정보를 불러오는 데 실패했습니다:', error);
    alert('호텔 정보를 불러오는 중 오류가 발생했습니다.');
  }
};


const editFormTitle = computed(() => selectedPropertyForEdit.value ? `"${selectedPropertyForEdit.value.name}" 숙소 관리` : `새 ${selectedPropertyType.value} 등록`);

const addNewProperty = () => {
  selectedPropertyForEdit.value = null;
  editableHotel.value = { 
    id: Date.now(), 
    name: '', 
    type: propertyTypeMapping[selectedPropertyType.value], 
    location: '', 
    stars: 0, 
    latitude: '', 
    longitude: '', 
    checkInTime: '15:00', 
    checkOutTime: '11:00', 
    image: '', 
    images: [], 
    rooms: [], 
    serviceIds: [] // 신규 등록 시에는 이미 배열로 초기화되어 있음
  }; 
  accommodationView.value = 'edit';
};

const triggerFileInput = (type, index) => { imageUpdateTarget.value = { type, index }; fileInputRef.value.click(); };

const removeRoom = (index) => { if (editableHotel.value) { editableHotel.value.rooms.splice(index, 1); } };
const fetchReviewsForHotel = async () => {
    if (!managedHotelId.value) {
        reviews.value = [];
        return;
    }
    try {
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/manager/reviews/hotel/${managedHotelId.value}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
        });
        reviews.value = response.data.map(review => ({ ...review, replyInput: '' }));
    } catch (error) {
        console.error("호텔 리뷰를 불러오는 데 실패했습니다:", error);
        reviews.value = [];
    }
};

const submitReply = async (review) => {
    if (!review.replyInput || !review.replyInput.trim()) {
        alert("답글 내용을 입력해주세요.");
        return;
    }
    try {
        await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/manager/reviews/${review.reviewId}/reply`, 
        { reply: review.replyInput }, 
        { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
        );
        alert("답글이 성공적으로 등록되었습니다.");
        fetchReviews();
    } catch (error) {
        console.error("답글 등록에 실패했습니다:", error);
        alert("답글 등록 중 오류가 발생했습니다.");
    }
};

const openDeletionModal = (review) => {
    requestingReview.value = review;
    deletionReason.value = '';
    isDeletionModalVisible.value = true;
};

const closeDeletionModal = () => {
    isDeletionModalVisible.value = false;
};

const submitDeletionRequest = async () => {
    if (!deletionReason.value.trim()) {
        alert('삭제 요청 사유를 입력해주세요.');
        return;
    }
    
    try {
        const response = await axios.post(
            `${import.meta.env.VITE_APP_API_URL}/api/manager/reviews/${requestingReview.value.reviewId}/request-deletion`,
            { reason: deletionReason.value },
            { headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` } }
        );
        alert(response.data.message || '삭제 요청이 성공적으로 접수되었습니다.');
        closeDeletionModal();
        fetchReviews();
    } catch (error) {
        console.error('리뷰 삭제 요청 실패:', error);
        const errorMessage = error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.';
        alert(errorMessage);
    }
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR');
  };

  watch(activeView, (newView) => {
      if (newView === 'reviews') {
          fetchReviews();
      }
  });

  onMounted(() => {
      if (activeView.value === 'reviews') {
          fetchReviews();
      }
  });

  const loadKakaoMapScript = () => {
    return new Promise((resolve) => {
      if (window.kakao && window.kakao.maps) {
        resolve();
        return;
      }
      const script = document.createElement('script');
      script.src = `//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=15c88964673a43f30cda3f5c892e15fb&libraries=services`;
      script.onload = () => kakao.maps.load(resolve);
      document.head.appendChild(script);
    });
  };

  const searchAddress = async () => {
    if (!editableHotel.value.location) {
      alert('주소를 입력해주세요.');
      return;
    }

    await loadKakaoMapScript();

    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.addressSearch(editableHotel.value.location, (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        editableHotel.value.latitude = result[0].y;
        editableHotel.value.longitude = result[0].x;
        alert('좌표가 성공적으로 입력되었습니다.');
      } else {
        alert('주소를 찾을 수 없습니다.');
      }
    });
  };

  const goHome = () => { activeView.value = 'dashboard'; };

  </script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nanum+Myeongjo:wght@700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;600;700;800&display=swap');

.manager-dashboard { display: grid; grid-template-columns: 260px 1fr; height: 100vh; font-family: 'Noto Sans KR', sans-serif; }
.manager-sidebar { background-color: #2C3E50; color: white; display: flex; flex-direction: column; padding: 25px; border-right: 1px solid #34495E; }
.sidebar-header .logo { font-family: 'Nanum Myeongjo', serif; font-size: 1.8rem; color: white; text-decoration: none; cursor: pointer; }
.sidebar-header .manager-mode { background-color: #3498DB; color: white; padding: 4px 8px; border-radius: 5px; font-size: 0.8rem; display: inline-block; margin-top: 10px; }
.sidebar-nav { margin-top: 40px; display: flex; flex-direction: column; gap: 10px; }
.sidebar-nav a { color: #ECF0F1; text-decoration: none; font-size: 1.1rem; padding: 15px 20px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; gap: 10px; transition: background-color 0.2s; }
.sidebar-nav a.active, .sidebar-nav a:hover { background-color: #34495E; }
.sidebar-footer { margin-top: auto; }
.btn-logout { width: 100%; padding: 12px; background-color: #E74C3C; color: white; border: none; border-radius: 8px; cursor: pointer; transition: background-color 0.2s; }
.btn-logout:hover { background-color: #c0392b; }
.manager-content { background-color: #F4F6F9; padding: 40px; overflow-y: auto; }
.content-header { margin-bottom: 30px; }
.content-header h1 { font-size: 2.2rem; font-weight: 800; color: #2C3E50; margin: 0; }
.content-header p { font-size: 1.1rem; color: #555; margin-top: 5px; }
.card { background-color: #fff; border-radius: 12px; padding: 30px; margin-bottom: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.card h4 { margin: 0 0 20px 0; font-size: 1.3rem; font-weight: 600; color: #333; }
.filter-controls { display: flex; gap: 10px; margin-bottom: 25px; flex-wrap: wrap;}
.filter-controls button { background-color: #fff; border: 1px solid #ddd; color: #555; padding: 8px 15px; border-radius: 20px; cursor: pointer; font-weight: 500; }
.filter-controls button.active { background-color: #2C3E50; color: white; border-color: #2C3E50; }
.dashboard-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 25px; }

.metric-card {
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
  display: flex;              /* 가로 정렬 */
  flex-direction: column;     /* 세로 쌓기 */
}
.metric-card h4 {
  text-align: left;           /* 제목 좌측 */
}
.metric-card .metric,
.metric-card .change-text {   /* 값과 증감률 */
  text-align: right;          /* 값 우측 */
}

.metric-card.active { border-color: #3498DB; box-shadow: 0 4px 20px rgba(52, 152, 219, 0.4); }
.card .metric { font-size: 2.5rem; font-weight: 700; color: #2C3E50; margin: 0; }
.property-type-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 25px; }
.property-type-card { text-align: center; cursor: pointer; border: 1px solid #eee; padding: 30px; border-radius: 12px; }
.property-type-card .icon { font-size: 3rem; }
.property-type-card h3 { font-size: 1.5rem; margin: 15px 0 5px 0; }
.btn-back { background: none; border: 1px solid #ccc; color: #555; font-weight: 600; border-radius: 8px; }
.property-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 25px; }
.property-card { padding: 0; display: flex; flex-direction: column; cursor: pointer; overflow: hidden; }
.property-image { width: 100%; height: 150px; object-fit: cover; }
.property-info { padding: 20px; flex-grow: 1; }
.property-manage-footer { background-color: #3498DB; color: white; padding: 12px; text-align: center; }
.add-new-card { align-items: center; justify-content: center; border-style: dashed; color: #aaa; }
.add-new-card .add-icon { font-size: 3rem; }
.edit-form-wrapper { max-width: 1200px; }
.edit-form-layout { display: grid; grid-template-columns: 1fr 320px; gap: 30px; align-items: flex-start; }
.form-main, .form-sidebar { min-width: 0; }
.sticky-sidebar { position: sticky; top: 40px; }
.save-panel { text-align: center; }
.save-panel p { margin: 15px 0; font-size: 0.95rem; color: #666; }
.btn-save { background-color: #27ae60; color: white; width: 100%; padding: 14px; font-size: 1.1rem; }
.btn-cancel { background-color: #f0f0f0; color: #555; width: 100%; padding: 14px; font-size: 1.1rem; }
.photo-management-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; }
.main-photo { grid-column: span 3; grid-row: span 2; }
.main-photo, .sub-photo { position: relative; border-radius: 8px; overflow: hidden; background-color: #f0f0f0; aspect-ratio: 4 / 3; }
.photo-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 3rem; color: #ccc; }
.main-photo img, .sub-photo img { width: 100%; height: 100%; object-fit: cover; }
.btn-photo-edit { position: absolute; bottom: 8px; right: 8px; background-color: rgba(0,0,0,0.5); color: white; border: none; padding: 5px 10px; font-size: 0.8rem; border-radius: 5px; cursor: pointer; }
.btn-photo-edit.small { padding: 4px 8px; font-size: 0.75rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-group.full-width { grid-column: span 2; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; }
.form-group .required { color: #E74C3C; }
input[type="text"], input[type="number"], input[type="time"], select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; }
.room-edit-card { display: flex; gap: 15px; align-items: center; margin-bottom: 15px; background-color: #f9f9f9; padding: 15px; border-radius: 8px; }
.room-photo { width: 100px; height: 75px; flex-shrink: 0; position: relative; border-radius: 6px; overflow: hidden; background-color: #e9e9e9; }
.room-photo .photo-placeholder.small { font-size: 1.5rem; }
.room-inputs-grid { flex-grow: 1; display: grid; grid-template-columns: repeat(3, 1fr); grid-template-rows: auto auto; gap: 15px; }
.form-group-small { display: flex; flex-direction: column; }
.form-group-small label { font-size: 0.85rem; font-weight: 500; color: #555; margin-bottom: 5px; }
.form-group-small input { padding: 8px; }
.room-name-input { grid-column: 1 / -1; }
.btn-remove-room { background-color: #E74C3C; color: white; padding: 8px 12px; font-size: 1rem; }
.btn-add-room { background: none; border: 1px dashed #ccc; width: 100%; padding: 12px; margin-top: 10px; color: #555; font-weight: 600; }
.amenities-checkbox-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.amenities-checkbox-grid label { display: flex; align-items: center; gap: 8px; font-size: 0.95rem; }
.review-list { display: flex; flex-direction: column; gap: 20px; }
.review-header { display: flex; align-items: flex-start; gap: 15px; margin-bottom: 10px; }
.review-hotel-name { font-size: 0.9rem; color: #888; margin-top: 4px; }
.review-rating { margin-left: auto; color: #F39C12; }
.review-actions textarea { height: 80px; width: 100%; box-sizing: border-box; margin-bottom: 10px; }
.action-buttons { display: flex; justify-content: flex-end; gap: 10px; }
.btn-delete-request { background-color: #E74C3C; color: white; }
button { padding: 12px 25px; font-size: 1rem; font-weight: 600; border-radius: 8px; border: none; cursor: pointer; background-color: #3498DB; color: white; }
.table-responsive { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; text-align: left; }
th, td { padding: 12px 15px; border-bottom: 1px solid #eee; vertical-align: middle; }
th { background-color: #f9fafb; font-weight: 600; color: #555; }
.no-results { text-align: center; color: #888; padding: 40px; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 0.85rem; font-weight: 600; color: white; display: inline-block; }
.status-confirmed { background-color: #27AE60; }
.status-pending { background-color: #F39C12; }
.status-cancelled { background-color: #E74C3C; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background-color: white; padding: 30px; border-radius: 12px; width: 90%; max-width: 500px; box-shadow: 0 5px 20px rgba(0,0,0,0.2); position: relative; }
.modal-close-btn { position: absolute; top: 15px; right: 15px; background: none; border: none; font-size: 1.8rem; cursor: pointer; color: #888; }
.modal-content h3 { margin-top: 0; margin-bottom: 20px; font-size: 1.5rem; color: #2C3E50; }
.booking-list { list-style: none; padding: 0; margin: 0; max-height: 400px; overflow-y: auto; }
.booking-list li { padding: 12px 0; border-bottom: 1px solid #eee; display: flex; align-items: center; gap: 10px; flex-wrap: wrap;}
.booking-list li:last-child { border-bottom: none; }
.booking-hotel-name { font-weight: 600; }
.booking-room-name { color: #555; }
.booking-guest-name { margin-left: auto; color: #888; font-size: 0.9rem; }

.hotel-card {
  cursor: pointer;
}
.hotel-card:hover {
  cursor: pointer;
  background-color: #f9f9f9;
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 9999px;
  flex-shrink: 0;
}
.card .flex-row {
  display: flex !important;
  flex-direction: row !important;
  align-items: flex-start;
}
.legend-list {
  min-width: 200px; /* 범례가 차트 밑으로 안 밀리게 최소 너비 보장 */
}
/* 버튼 */
button {
  margin-left: 8px;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 0.9rem;
  background-color: #f9fafb;
  border: 1px solid #d1d5db;
  color: #374151;
  transition: all 0.2s;
}
button:hover {
  background-color: #e5e7eb;
}
button.active {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
  font-weight: 600;
}

/* 호텔 카드 */
.hotel-card.active {
  border: 2px solid #3498db;
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.3);
  background-color: #f0f9ff;
}

/* 도넛 범례 dot */
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 9999px;
  flex-shrink: 0;
}

/* --- 새로운 스타일 --- */
.search-card { padding-bottom: 15px; }
.search-controls { display: flex; gap: 10px; }
.search-select { padding: 10px; border-radius: 6px; border: 1px solid #ccc; font-weight: 500; }
.search-input { flex-grow: 1; padding: 10px; border-radius: 6px; border: 1px solid #ccc; }
.data-policy-note { font-size: 0.85rem; color: #888; margin-top: 15px; text-align: right; }
/* [추가] 예약 상태 필터 스타일 */
.status-filter-controls { display: flex; gap: 10px; margin-top: 20px; color: #555; border-top: 1px solid #eee; padding-top: 20px; }
.status-filter-controls button { background-color: #fff; border: 1px solid #ddd; color: #555; padding: 8px 15px; border-radius: 20px; cursor: pointer; font-weight: 500; }
.status-filter-controls button.active { background-color: #3498DB; color: white; border-color: #3498DB; }

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.filter-tabs button {
  padding: 8px 18px;
  border-radius: 20px;
  border: 1px solid #ddd;
  color: #555;
  background-color: #fff;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-tabs button:hover {
  background-color: #f0f0f0;
}
.filter-tabs button.active {
  background-color: #2C3E50;
  color: white;
  border-color: #2C3E50;
}

.user-type-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 700;
  margin-right: 8px;
  color: white;
  vertical-align: middle;
}
.user-type-badge.member { background-color: #27AE60; } /* 초록색 */
.user-type-badge.guest { background-color: #F39C12; } /* 주황색 */


.review-list { display: flex; flex-direction: column; gap: 25px; }
.review-card { padding: 25px; display: flex; flex-direction: column; gap: 15px; border: 1px solid #e0e7ff; transition: box-shadow 0.2s ease-in-out; }
.review-card:hover { box-shadow: 0 8px 25px rgba(0,0,0,0.08); }
.review-header { display: flex; justify-content: space-between; align-items: flex-start; }
.review-info { display: flex; flex-direction: column; gap: 4px; }
.review-user-name { font-size: 1.2rem; font-weight: 700; color: #2C3E50; }
.review-hotel-name { font-size: 0.9rem; color: #555; }
.review-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.review-date { font-size: 0.85rem; color: #888; flex-shrink: 0; }
.review-rating { color: #E0E0E0; font-size: 1.2rem; }
.review-rating .star.filled { color: #F39C12; }
.review-content { line-height: 1.7; color: #333; font-size: 1rem; padding: 20px; background-color: #f8f9fa; border-radius: 8px; border: 1px solid #eee; }
.review-reply-section { margin-top: 10px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.manager-reply { background-color: #f1f5f9; border-radius: 8px; padding: 20px; border-left: 4px solid #4A69A1; }
.reply-header { font-size: 0.9rem; color: #555; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }
.reply-header strong { color: #4A69A1; font-size: 1rem; }
.reply-content { line-height: 1.6; color: #333; white-space: pre-wrap; }
.reply-form textarea { width: 100%; box-sizing: border-box; border: 1px solid #ccc; border-radius: 6px; padding: 12px; margin-bottom: 10px; resize: vertical; min-height: 100px; font-family: 'Noto Sans KR', sans-serif; font-size: 0.95rem; transition: border-color 0.2s, box-shadow 0.2s;}
.reply-form textarea:focus { border-color: #4A69A1; outline: none; box-shadow: 0 0 0 3px rgba(74, 105, 161, 0.2); }
.reply-actions { display: flex; justify-content: flex-end; gap: 10px; }
.btn-delete-request { background-color: #9ca3af; color: white; font-size: 0.9rem; padding: 8px 16px; transition: background-color 0.2s; border: none; border-radius: 6px; cursor: pointer;}
.btn-delete-request:hover { background-color: #6b7280; }
.btn-submit-reply { background-color: #27ae60; color: white; font-size: 0.9rem; padding: 8px 16px; transition: background-color 0.2s; border: none; border-radius: 6px; cursor: pointer;}
.btn-submit-reply:disabled { background-color: #ccc; cursor: not-allowed; }
.btn-submit-reply:hover:not(:disabled) { background-color: #219d52; }
.no-reviews-card { text-align: center; color: #888; font-size: 1.1rem; padding: 50px; }

/* 삭제 요청 상태 표시 스타일 */
.deletion-status-section { margin-top: 15px; padding: 15px; border-radius: 8px; border-left-width: 4px; border-left-style: solid; }
.status-rejected { background-color: #fff5f5; border-left-color: #e53e3e; }
.status-pending { background-color: #fffaf0; border-left-color: #f6ad55; }
.deletion-status-section .status-header { font-size: 1rem; font-weight: 700; margin: 0 0 5px 0; }
.status-rejected .status-header { color: #c53030; }
.status-pending .status-header { color: #dd6b20; }
.deletion-status-section .rejection-reason { font-size: 0.9rem; color: #4a5568; margin: 0; }
.rejection-reason strong { font-weight: 600; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background-color: #fff; border-radius: 12px; width: 90%; max-width: 500px; box-shadow: 0 5px 25px rgba(0,0,0,0.2); }
.modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h2 { margin: 0; font-size: 1.5rem; }
.btn-close { background: none; border: none; font-size: 2rem; cursor: pointer; color: #888; }
.modal-body { padding: 25px; }
.review-preview-card { background-color: #f9f9f9; padding: 15px; border-radius: 8px; margin-bottom: 20px; text-align: left;}
.review-text-in-modal { font-style: italic; color: #555; }
.form-group { text-align: left; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; }
.form-group textarea { width: 100%; box-sizing: border-box; border: 1px solid #ccc; border-radius: 6px; padding: 12px; resize: vertical; min-height: 120px; font-family: 'Noto Sans KR', sans-serif; font-size: 0.95rem; }
.modal-footer { padding: 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.modal-footer button { padding: 10px 20px; font-weight: 600; border-radius: 6px; border: none; cursor: pointer; }
.modal-footer .btn-cancel { background-color: #f0f0f0; color: #555; border: 1px solid #ddd; }
.modal-footer .btn-submit-request { background-color: #E74C3C; color: white; }
.modal-footer .btn-submit-request:disabled { background-color: #ccc; cursor: not-allowed; }
/* 대기 카드 스타일 */
.pending-card {
  background-color: #f3f4f6; /* 연한 회색 배경 */
  opacity: 0.8;
  position: relative;
}

/* 상태 뱃지 */
.status-badge {
  display: inline-block;
  margin-top: 5px;
  padding: 4px 8px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #fff;
  background-color: #9ca3af; /* 회색 */
  border-radius: 6px;
}
.account-info p {
    font-size: 1.1rem;
    line-height: 1.8;
    margin-bottom: 5px;
    color: #374151;
}
.account-info strong {
    font-weight: 700;
    color: #111827;
    display: inline-block;
    width: 80px; /* 라벨 너비를 고정하여 정렬 */
}

/* 비밀번호 변경 폼 컨테이너 */
.password-form {
    max-width: 450px; /* 폼 너비 제한 */
    margin-top: 20px;
    padding: 0;
}

/* 폼 그룹 간격 */
.form-group {
    margin-bottom: 20px;
}

/* 폼 그룹 라벨 스타일 */
.form-group label {
    display: block;
    font-weight: 600;
    margin-bottom: 8px;
    color: #374151;
}

/* 입력 필드 스타일 */
.form-group input {
    width: 100%;
    padding: 10px 15px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 1rem;
    box-sizing: border-box;
}

/* 버튼 스타일 (기존 .btn-sm .btn-approve 사용) */
.password-form button[type="submit"] {
    margin-top: 10px;
    width: auto; /* 버튼이 너무 넓어지지 않도록 조정 */
    padding: 10px 25px;
    background-color: #10B981; /* btn-approve */
    color: white;
    font-size: 1rem;
    border: none;
}


</style>