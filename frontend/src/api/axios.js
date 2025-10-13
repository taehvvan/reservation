import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: 'http://localhost:8888', // 백엔드 기본 URL
  headers: {
    'Content-Type': 'application/json',
  },
});

// 1. 요청 인터셉터: 모든 요청이 보내지기 전에 실행됩니다.
apiClient.interceptors.request.use(
  (config) => {
    // localStorage에서 토큰을 가져옵니다.
    const token = localStorage.getItem('accessToken');
    
    // 토큰이 존재하면 Authorization 헤더에 추가합니다.
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 2. 응답 인터셉터: 모든 응답을 받은 후에 실행됩니다.
apiClient.interceptors.response.use(
  (response) => {
    // 정상 응답은 그대로 반환
    return response;
  },
  (error) => {
    // 응답이 401 에러(인증 실패)인 경우, 자동으로 로그아웃 처리
    if (error.response && error.response.status === 401) {
      const authStore = useAuthStore();
      console.error("인증 실패 또는 토큰 만료. 자동 로그아웃됩니다.", error);
      authStore.logout(); // authStore의 logout 액션 호출
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
    }
    return Promise.reject(error);
  }
);

export default apiClient;

