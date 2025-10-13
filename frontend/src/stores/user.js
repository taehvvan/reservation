// src/stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null') // 로그인 정보 저장
  }),
  actions: {
    setUser(userData) {
      this.user = userData
      localStorage.setItem('user', JSON.stringify(userData))
    },
    clearUser() {
      this.user = null
      localStorage.removeItem('user')
    }
  }
})
