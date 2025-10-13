import axios from 'axios'

// 백엔드 API 서버의 기본 주소를 설정합니다.
const apiClient = axios.create({
  baseURL: 'http://localhost:8888',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Axios 요청 인터셉터
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 관리자가 사용자 목록을 조회하는 API
 * @param {object} params - { role: 'USER'/'MANAGER'/'ALL', searchQuery: '검색어' }
 */
export const fetchAdminUsers = (params) => {
  return apiClient.get('/api/admin/users', { params })
}

/**
 * 관리자가 사용자를 삭제하는 API
 * @param {number} userId - 삭제할 사용자의 ID
 */
export const deleteAdminUser = (userId) => {
  return apiClient.delete(`/api/admin/users/${userId}`)
}

/**
 * 관리자가 호텔 목록을 조회하는 API
 * @param {string|string[]} type - 호텔 타입 ('HOTEL', 'MOTEL', 'PENSION' 등)
 */
export const fetchAdminHotels = (type) => {
  // 단일 타입이면 문자열, 여러 타입이면 배열로 전송
  const params = Array.isArray(type) ? { type } : { type: type }
  return apiClient.get('/api/admin/hotels', { params })
}

/**
 * 관리자가 특정 호텔을 비활성화(삭제/숨김)하는 API
 * @param {number} hotelId - 호텔 ID
 */
export const disableAdminHotel = (hotelId) => {
  return apiClient.patch(`/api/admin/hotels/${hotelId}/disable`)
}

export const approveAdminManager = (userId) => {
  return apiClient.post(`/api/admin/users/${userId}/approve`);
}
