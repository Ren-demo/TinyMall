import { defineStore } from 'pinia'
import api from '../services/api'
import router from '../router'

const normalizeAddress = (val) => {
  if (!val) return ''
  const s = String(val).trim()
  if (!s || s === '(暂无)') return ''
  return s
}

function isPersistentPicture(url) {
  if (!url) return false
  try {
    const s = String(url).trim()
    if (!s) return false
    // Accept http(s) or root-relative paths. Reject blob: and data: temporary URLs.
    if (s.startsWith('http://') || s.startsWith('https://') || s.startsWith('/')) return true
    return false
  } catch (e) { return false }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // Prefer sessionStorage (temporary supplier sessions) over localStorage persistent login
    token: sessionStorage.getItem('token') || localStorage.getItem('token') || '',
    role: sessionStorage.getItem('role') || localStorage.getItem('role') || '', // 'user' or 'supplier'
    username: sessionStorage.getItem('username') || localStorage.getItem('username') || '',
    email: sessionStorage.getItem('email') || localStorage.getItem('email') || '',
    userId: sessionStorage.getItem('userId') || localStorage.getItem('userId') || '',
    password: sessionStorage.getItem('password') || localStorage.getItem('password') || '',
    userPicture: (function() {
      const s = sessionStorage.getItem('userPicture') || localStorage.getItem('userPicture') || ''
      return isPersistentPicture(s) ? s : ''
    })(),
    address: normalizeAddress(sessionStorage.getItem('address') || localStorage.getItem('address') || '')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async login({ email, password, role }) {
      const res = await api.login({ email, password, role })
      if (res.success) {
        this.token = res.token
        // 优先使用后端返回的角色/用户名
        this.role = res.role || role
        this.username = res.username || email
        this.email = res.email || email
        this.userId = res.userId || ''
        this.password = password || ''
        // Only accept persistent URLs from backend; avoid accepting temporary blob: URLs
        const candidate = res.userPicture || this.userPicture || ''
        this.userPicture = isPersistentPicture(candidate) ? candidate : ''
        this.address = normalizeAddress(res.address) || this.address || ''
        // 同步到 sessionStorage，避免部分环境 localStorage 受限
        sessionStorage.setItem('token', res.token)
        sessionStorage.setItem('role', this.role)
        sessionStorage.setItem('username', this.username)
        sessionStorage.setItem('email', this.email)
        if (this.userId) sessionStorage.setItem('userId', this.userId)
        if (this.password) sessionStorage.setItem('password', this.password)
        if (this.userPicture && isPersistentPicture(this.userPicture)) sessionStorage.setItem('userPicture', this.userPicture)
        if (this.address) sessionStorage.setItem('address', this.address)
        // persistent login stored in localStorage
        localStorage.setItem('token', res.token)
        localStorage.setItem('role', this.role)
        localStorage.setItem('username', this.username)
        localStorage.setItem('email', this.email)
        if (this.userId) localStorage.setItem('userId', this.userId)
        if (this.password) localStorage.setItem('password', this.password)
        if (this.userPicture && isPersistentPicture(this.userPicture)) localStorage.setItem('userPicture', this.userPicture)
        if (this.address) localStorage.setItem('address', this.address)
        console.log('✓ Token 已保存到 localStorage')
        console.log('  - 用户名:', this.username)
        console.log('  - 角色:', this.role)
        console.log('  - Token 长度:', res.token.length, '字节')
      }
      return res
    },
    logout() {
      this.token = ''
      this.role = ''
      this.username = ''
      this.email = ''
      this.userId = ''
      this.password = ''
      this.userPicture = ''
      this.address = ''
      // clear both persistent and session storage
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      localStorage.removeItem('email')
      localStorage.removeItem('userId')
      localStorage.removeItem('password')
      localStorage.removeItem('userPicture')
      localStorage.removeItem('address')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('role')
      sessionStorage.removeItem('username')
      sessionStorage.removeItem('email')
      sessionStorage.removeItem('userId')
      sessionStorage.removeItem('password')
      sessionStorage.removeItem('userPicture')
      sessionStorage.removeItem('address')
      router.push('/login')
    }
  }
})