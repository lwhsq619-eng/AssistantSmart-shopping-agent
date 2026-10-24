import { defineStore } from 'pinia'
import { adminLoginApi, userLoginApi, registerApi } from '../api/auth'

const storageKey = 'mall_auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    auth: JSON.parse(localStorage.getItem(storageKey) || 'null')
  }),
  getters: {
    user: (state) => state.auth?.user || null,
    token: (state) => state.auth?.token || '',
    role: (state) => state.auth?.user?.role || '',
    isLogin: (state) => !!state.auth?.token
  },
  actions: {
    persist(loginVO) {
      this.auth = {
        token: loginVO.token,
        user: loginVO
      }
      localStorage.setItem(storageKey, JSON.stringify(this.auth))
    },
    async adminLogin(form) {
      const data = await adminLoginApi(form)
      this.persist(data)
      return data
    },
    async userLogin(form) {
      const data = await userLoginApi(form)
      this.persist(data)
      return data
    },
    async register(form) {
      return registerApi(form)
    },
    logout() {
      this.auth = null
      localStorage.removeItem(storageKey)
      localStorage.removeItem('mall_user')
    }
  }
})
