import { defineStore } from 'pinia'
import { toRaw } from 'vue';
import axios from 'axios'

export const useBookingStore = defineStore('booking', {
  state: () => ({
    search: JSON.parse(localStorage.getItem('booking_search') || '{}'),
    hotel: JSON.parse(localStorage.getItem('booking_hotel') || 'null') || null,
    room: JSON.parse(localStorage.getItem('booking_room') || 'null') || null,
    reservationId: null,
    // ✅ 날짜는 string → Date 객체로 복원
    checkIn: localStorage.getItem('booking_checkIn') ? new Date(localStorage.getItem('booking_checkIn')) : null,
    checkout: localStorage.getItem('booking_checkout') ? new Date(localStorage.getItem('booking_checkout')) : null,
    // ✅ 숫자 값은 Number()로 변환
    guests: localStorage.getItem('booking_guests') ? Number(localStorage.getItem('booking_guests')) : null,
    rooms: localStorage.getItem('booking_rooms') ? Number(localStorage.getItem('booking_rooms')) : 1,
  }),

  actions: {
    setBookingDetails(details) {
      this.hotel = details.hotel;
      this.room = details.room;
      // ✅ checkIn/checkout은 string이 아닌 Date 객체로 저장
      this.checkIn = details.checkIn ? new Date(details.checkIn) : null;
      this.checkout = details.checkout ? new Date(details.checkout) : null;
      this.guests = Number(details.guests);
      this.rooms = Number(details.rooms);

      localStorage.setItem('booking_hotel', JSON.stringify(this.hotel));
      localStorage.setItem('booking_room', JSON.stringify(this.room));
      localStorage.setItem('booking_checkIn', this.checkIn ? this.checkIn.toISOString().split('T')[0] : '');
      localStorage.setItem('booking_checkout', this.checkout ? this.checkout.toISOString().split('T')[0] : '');
      localStorage.setItem('booking_guests', this.guests);
      localStorage.setItem('booking_rooms', this.rooms);

      sessionStorage.setItem('bookingState', JSON.stringify(this.$state));
    },

    setBooking(search, hotel, room) {
      this.search = search;
      this.hotel = toRaw(hotel);
      this.room = room;

      localStorage.setItem('booking_search', JSON.stringify(search));
      localStorage.setItem('booking_hotel', JSON.stringify(hotel));
      localStorage.setItem('booking_room', JSON.stringify(room));

      console.log('Booking set:', this.search, this.hotel, this.room);
    },

    setReservationId(id) {
      this.reservationId = id;
      console.log('Reservation ID set:', id);
    },

    clearBooking() {
      this.search = {};
      this.hotel = null;
      this.room = null;
      this.reservationId = null;
      this.checkIn = null;
      this.checkout = null;
      this.guests = null;
      this.rooms = 1;

      localStorage.removeItem('booking_search');
      localStorage.removeItem('booking_hotel');
      localStorage.removeItem('booking_room');
      localStorage.removeItem('booking_checkIn');
      localStorage.removeItem('booking_checkout');
      localStorage.removeItem('booking_guests');
      localStorage.removeItem('booking_rooms');

      console.log('Booking cleared');
    },

    clearLocalStorage() {
      localStorage.removeItem('booking_search');
      localStorage.removeItem('booking_hotel');
      localStorage.removeItem('booking_room');
    },

    async fetchBookingDetails() {
      console.log("데이터 복구를 시도합니다. (fetchBookingDetails 호출됨)");

      if (this.hotel && this.room) {
        console.log("State에 데이터가 이미 존재하여 fetch를 건너뜁니다.");
        return;
      }

      const storedHotel = JSON.parse(localStorage.getItem('booking_hotel') || 'null');
      const storedRoom = JSON.parse(localStorage.getItem('booking_room') || 'null');

      // ✅ 대소문자 정확히 (hId / rId)
      if (!storedHotel?.hId || !storedRoom?.rId) {
        this.clearBooking();
        throw new Error("localStorage에 예약 정보를 불러올 ID가 없습니다.");
      }

      const hotelId = storedHotel.hId;
      const roomId = storedRoom.rId;

      try {
        console.log(`ID [hotel: ${hotelId}, room: ${roomId}]로 서버에 데이터 요청...`);

        const [hotelResponse, roomResponse] = await Promise.all([
          axios.get(`${import.meta.env.VITE_APP_API_URL}/api/hotels/${hotelId}`),
          axios.get(`${import.meta.env.VITE_APP_API_URL}/api/rooms/${roomId}`) 
        ]);

        this.hotel = hotelResponse.data;
        this.room = roomResponse.data;

        // ✅ localStorage → Date/Number 변환
        this.checkIn = localStorage.getItem('booking_checkIn') ? new Date(localStorage.getItem('booking_checkIn')) : null;
        this.checkout = localStorage.getItem('booking_checkout') ? new Date(localStorage.getItem('booking_checkout')) : null;
        this.guests = localStorage.getItem('booking_guests') ? Number(localStorage.getItem('booking_guests')) : null;
        this.rooms = localStorage.getItem('booking_rooms') ? Number(localStorage.getItem('booking_rooms')) : 1;

        localStorage.setItem('booking_hotel', JSON.stringify(this.hotel));
        localStorage.setItem('booking_room', JSON.stringify(this.room));

        console.log("✅ 데이터 복구 및 state 업데이트 완료!");

      } catch (error) {
        console.error("서버에서 예약 정보를 불러오는 중 오류 발생:", error);
        this.clearBooking();
        throw error;
      }
    }
  }
});