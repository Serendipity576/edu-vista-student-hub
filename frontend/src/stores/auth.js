import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/utils/axios'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  
  try {
    const userStr = localStorage.getItem('user')
    if (userStr && userStr !== 'null') {
      user.value = JSON.parse(userStr)
    }
  } catch (e) {
    console.error('Failed to parse user from localStorage', e)
    localStorage.removeItem('user')
  }
  
  const isAuthenticated = computed(() => !!token.value)
  
  const login = async (username, password) => {
    try {
      const response = await axios.post('/auth/login', { username, password })
      if (response.data.code === 200) {
        token.value = response.data.data.token
        user.value = {
          username: response.data.data.username,
          role: response.data.data.role,
          avatar: response.data.data.avatar
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
        return { success: true }
      }
      return { success: false, message: response.data.message }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '登录失败' }
    }
  }
  
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete axios.defaults.headers.common['Authorization']
  }
  
  const register = async (username, password, email) => {
    try {
      const response = await axios.post('/auth/register', { username, password, email })
      if (response.data.code === 200) {
        return { success: true, message: '注册成功' }
      }
      return { success: false, message: response.data.message }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '注册失败' }
    }
  }
  
  const initAuth = () => {
    if (token.value) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    }
  }
  
  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
    register,
    initAuth
  }
})

