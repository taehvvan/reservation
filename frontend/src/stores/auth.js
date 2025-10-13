import { defineStore } from 'pinia';
import axios from 'axios'; 
import router from '@/router';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    userName: '',
    userRole: '',
    userId: null,
    userEmail: '',
    isLoading: true,
  }),
  actions: {
    async login(email, password, role = null) {
      try {
        const payload = { email, password };
        if (role) {
          payload.role = role; // 매니저 로그인 시 role 추가
        }

        const response = await axios.post(`${import.meta.env.VITE_APP_API_URL}/api/auth/login`, payload);

        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        console.log('로그인 성공, Access Token:', response.data.accessToken);

        // 토큰 저장 후 사용자 정보 가져오기
        await this.fetchUserInfo();

        // [수정] 역할(Role)에 따라 페이지 이동
        if (this.userRole === 'ROLE_ADMIN') {
          router.push('/admin');
        } else if (this.userRole === 'ROLE_MANAGER') {
          router.push('/manager');
        } else {
          router.push('/');
        }

      } catch (error) {
        console.error('로그인 실패:', error.response ? error.response.data : error);
        alert('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
        // 실패 시 에러를 다시 던져서 컴포넌트에서 후속 처리를 할 수 있게 함
        throw error;
      }
    },

    async fetchUserInfo() {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        this.logoutAction(); // 내부적으로 사용할 로그아웃 액션
        this.isLoading = false;
        return;
      }

      try {
        const response = await axios.get(`${import.meta.env.VITE_APP_API_URL}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        
        this.isLoggedIn = true;
        this.userName = response.data.name;
        this.userRole = response.data.role;
        this.userId = response.data.id;
        this.userEmail = response.data.email;
    
        console.log('사용자 정보 업데이트 완료:', { 
            name: this.userName, 
            role: this.userRole, 
            id: this.userId 
        });
    
      } catch (err) {
        console.error('사용자 정보 조회 실패:', err);
        this.logoutAction(); // 토큰이 유효하지 않으면 로그아웃 처리
      }
    },

    async checkLoginStatus() {
      this.isLoading = true;
      const token = localStorage.getItem('accessToken');
      if (token) {
        await this.fetchUserInfo();
      } else {
        this.isLoggedIn = false;
        this.userName = '';
        this.userRole = '';
        this.userId = null;
        this.userEmail = '';
      }
      this.isLoading = false;
    },

    // 다른 컴포넌트에서 호출할 공용 로그아웃 메서드
    logout() {
      this.logoutAction();
      alert('로그아웃 되셨습니다.');
      router.push('/');
    },

    // 내부 상태 변경 및 정리만 담당하는 메서드
    logoutAction() {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('paymentInfo');
      this.isLoggedIn = false;
      this.userName = '';
      this.userRole = '';
      this.userId = null;
    }
  }
});