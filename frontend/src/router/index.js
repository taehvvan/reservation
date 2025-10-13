// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import MainPage from '../components/MainPage.vue';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import AccommodationListPage from '../components/AccommodationListPage.vue';
import LandmarkListPage from '../components/LandmarkListPage.vue';
import HeritageListPage from '../components/HeritageListPage.vue';
import WishlistPage from '../components/WishlistPage.vue';
import BookingCheckPage from '../components/BookingCheckPage.vue';
import SearchResultPage from '../components/SearchResultPage.vue';
import HotelDetailPage from '../components/HotelDetailPage.vue'; 
import UserMypage from '../components/UserMypage.vue';
import TermsOfService from '../components/TermsOfService.vue';
import PrivacyPolicy from '../components/PrivacyPolicy.vue';
import CheckoutPage from '../components/CheckoutPage.vue';
import CheckoutPageGuest from '../components/CheckoutPageGuest.vue';
import HotelManagerPage from '../components/HotelManagerPage.vue';
import AdminDashboardPage from '../components/AdminDashboardPage.vue';
import LandmarkDetailPage from '../components/LandmarkDetailPage.vue';
import HeritageDetailPage from '../components/HeritageDetailPage.vue';
import RegisterSuccess from '../components/RegisterSuccess.vue';
import ManagerRegisterPage from '../components/ManagerRegisterPage.vue';
import ManagerLoginPage from '../components/ManagerLoginPage.vue';
import KakaoCallback from '../components/KakaoCallback.vue';
import GoogleCallback from '../components/GoogleCallback.vue';
import PasswordReset from '@/components/PasswordReset.vue';
import PaymentSuccess from '../components/PaymentSuccess.vue';
import PaymentFail from '../components/PaymentFail.vue';
import PaymentCallback from '../components/PaymentCallback.vue';
import GuestBookingDetail from '../components/GuestBookingDetail.vue';
import ManagerPendingPage from '../components/ManagerPendingPage.vue'; // [추가]
import InquiryListPage from '@/components/InquiryListPage.vue';
import InquiryCreatePage from '@/components/InquiryCreatePage.vue';
import InquiryDetailPage from '@/components/InquiryDetailPage.vue';
import InquiryBoardPage from '@/components/InquiryBoardPage.vue';

const routes = [
  // --- 공용 페이지 ---
  { path: '/', name: 'MainPage', component: MainPage },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/register-success', name: 'RegisterSuccess', component: RegisterSuccess, props: true },
  { path: '/manager-register', name: 'ManagerRegister', component: ManagerRegisterPage },
  { path: '/manager-pending', name: 'ManagerPending', component: ManagerPendingPage }, // [추가]
  { path: '/manager-login', name: 'ManagerLogin', component: ManagerLoginPage },
  { path: '/search', name: 'SearchResult', component: SearchResultPage },
  { path: '/hotel/:id', name: 'HotelDetail', component: HotelDetailPage, props: true },
  { path: '/landmark/:id', name: 'LandmarkDetail', component: LandmarkDetailPage, props: true },
  { path: '/heritage/:id', name: 'HeritageDetail', component: HeritageDetailPage, props: true },
  { path: '/terms', name: 'TermsOfService', component: TermsOfService },
  { path: '/privacy', name: 'PrivacyPolicy', component: PrivacyPolicy },
  { path: '/accommodations', name: 'AccommodationList', component: AccommodationListPage },
  { path: '/landmarks', name: 'LandmarkList', component: LandmarkListPage },
  { path: '/heritage', name: 'HeritageList', component: HeritageListPage },
  { path: '/kakao/callback', name: 'kakaoCallback', component: KakaoCallback },
  { path: '/google/callback', name: 'googleCallback', component: GoogleCallback },
  { path: '/password-reset', name: 'PasswordReset', component: PasswordReset},
  { path: '/payment-fail', name: 'PaymentFail', component: PaymentFail },
  { path: '/payment-callback', name: 'PaymentCallback', component: PaymentCallback, meta: { requiresAuth: false} },
  { path: '/payment-success', name: 'PaymentSuccess', component: PaymentSuccess },
  { path: '/booking-detail', name: 'GuestBookingDetail', component: GuestBookingDetail },
  
  {
    path: '/inquiry',
    name: 'InquiryList',
    component: InquiryListPage,
    meta: { requiresAuth: true, roles: ['ADMIN'] } // 관리자만 접근 가능
  },
  {
    path: '/inquiry/create',
    name: 'InquiryCreate',
    component: InquiryCreatePage
  },
  {
    path: '/inquiry/:id',
    name: 'InquiryDetail',
    component: InquiryDetailPage,
    props: true
  },
  {
    path: '/inquiries/board', // [추가] 문의 게시판 경로
    name: 'InquiryBoard',
    component: InquiryBoardPage,
  },

  // --- 일반 사용자 전용 페이지 (로그인 필요) ---
  { path: '/mypage', name: 'UserMypage', component: UserMypage, meta: { requiresAuth: true } },
  { path: '/wishlist', name: 'Wishlist', component: WishlistPage, meta: { requiresAuth: true } },
  { path: '/booking-check', name: 'BookingCheck', component: BookingCheckPage, meta: { requiresAuth: true } },
  {
    path: '/checkout',
    name: 'Checkout',
    component: CheckoutPage,
    meta: { requiresAuth: true },
    props: (route) => ({
      hotelName: route.query.hotelName,
      roomName: route.query.roomName,
      price: Number(route.query.price)
    })
  },

  // --- 비회원 전용 페이지 ---
  {
    path: '/checkout-guest',
    name: 'CheckoutGuest',
    component: CheckoutPageGuest,
    props: (route) => ({
      hotelName: route.query.hotelName,
      roomName: route.query.roomName,
      basePrice: Number(route.query.basePrice)
    })
  },

  {
      path: '/guest/booking/:orderId',
      name: 'GuestBookingDetail',
      component: GuestBookingDetail,
      props: true // URL 파라미터(:orderId)를 컴포넌트의 props로 전달합니다.
  },

  // --- 호텔 매니저 전용 페이지 ---
  {
    path: '/manager',
    name: 'HotelManager',
    component: HotelManagerPage,
    meta: { requiresAuth: true, requiresRole: 'MANAGER' }
  },

  // --- 사이트 관리자 전용 페이지 ---
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboardPage,
    meta: { requiresAuth: true, requiresRole: 'ADMIN' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // 1. 결제 콜백은 인증 없이 접근 허용
  if (to.path === '/payment-callback') {
    return next();
  }

  // 로그인 상태 복구
  if (!authStore.isLoggedIn) {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        await authStore.fetchUserInfo(token);
      } catch (err) {
        console.error('토큰 재검증 실패:', err);
      }
    }
  }

  // ROLE 기반 접근 제한
  const publicManagerRoutes = ['/manager-login', '/manager-register', '/manager-pending'];
  if (to.path.startsWith('/manager') && !publicManagerRoutes.includes(to.path) && authStore.userRole !== 'ROLE_MANAGER') {
    return next('/');
  }

  next();
});

export default router;
