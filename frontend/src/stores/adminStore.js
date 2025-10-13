import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchAdminUsers, deleteAdminUser, fetchAdminHotels, approveAdminManager } from '@/api/admin.js'


export const useAdminStore = defineStore('admin', () => {
  const users = ref([]);
  const hotels = ref([]);
  const isLoading = ref(false);
  // 사용자 목록 조회
  async function getUsers(params) {
    isLoading.value = true;
    try {
      const response = await fetchAdminUsers(params);
      users.value = response.data;
    } catch (error) {
      console.error('관리자용 사용자 목록 조회 실패:', error);
      users.value = [];
    } finally { isLoading.value = false; }
  }

  // 사용자 삭제
  async function removeUser(userId) {
    try {
      await deleteAdminUser(userId);
      await getUsers({ role: 'ALL', searchQuery: '' });
      alert('사용자가 성공적으로 삭제되었습니다.');
    } catch (error) {
      console.error('사용자 삭제 실패:', error);
      alert('사용자 삭제에 실패했습니다.');
    }
  }

   async function approveManager(userId) {
    try {
      await approveAdminManager(userId);
      // 승인 후 사용자 목록을 다시 불러옵니다.
      await getUsers({ role: 'ALL', searchQuery: '' }); 
      alert('매니저 가입이 성공적으로 승인되었습니다.');
    } catch (error) {
      console.error('매니저 승인 실패:', error);
      alert('매니저 승인 처리에 실패했습니다.');
    }
  }

  return { 
    users, 
    hotels, 
    isLoading, 
    getUsers, 
    removeUser, 
    getHotels, 
    updateHotelStatus,
    approveManager // [추가] approveManager 반환
  };


  // 호텔 목록 조회
  async function getHotels(type) {
    isLoading.value = true;
    try {
      const response = await fetchAdminHotels(type);
      hotels.value = response.data.map(h => ({
        hId: h.hid,        // 소문자 hid → Vue에서는 hId
        hName: h.hname,    // 소문자 hname → Vue에서는 hName
        type: h.type,
        region: h.region,
        address: h.address,
        latitude: h.latitude,
        longitude: h.longitude,
        star: h.star,
        active: h.active,
        info: h.info,
        images: h.images || [],
        rooms: h.rooms || [],
        reviews: h.reviews || [],
        services: h.services || [],
        monthlyRevenue: h.monthlyRevenue || 0,
        bookingCount: h.bookingCount || 0,
        averageRating: h.averageRating || 0
      }));
    } catch (error) {
      console.error('호텔 목록 조회 실패:', error);
      hotels.value = [];
    } finally { isLoading.value = false; }
  }

  // 호텔 상태 변경
  async function updateHotelStatus(hId, newStatus) {
    try {
      // 백엔드 API 호출 (PUT)
      await fetch(`/api/admin/hotels/${hId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ active: newStatus })
      });

      // 로컬 hotels 배열 업데이트
      const hotel = hotels.value.find(h => h.hId === hId);
      if (hotel) hotel.active = newStatus;

      alert('호텔 상태가 변경되었습니다.');
    } catch (error) {
      console.error('호텔 상태 변경 실패:', error);
      alert('호텔 상태 변경에 실패했습니다.');
    }
  }

  return { users, hotels, isLoading, getUsers, removeUser, getHotels, updateHotelStatus };




  
});

